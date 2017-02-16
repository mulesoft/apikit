/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

import groovy.transform.Field

@Field String pomName = 'pom.xml'
@Field String whitelistName = 'assembly-whitelist.txt'
@Field String assemblyWhitelistPath = 'distributions/standalone/'

@Field String whitelistProductVersion = '${productVersion}'
@Field String pomProjectVersion = '${project.version}'
@Field File root
@Field Map dependency

@Field dependencyName = ""
@Field dependenciesVersionFrom = ""
@Field dependenciesVersionTo = ""
@Field whitelistDependenciesVersionFrom = ""
@Field whitelistDependenciesVersionTo = ""

@Field ConfigObject dependencies = [
        CORS : [
                pomPath        : '.',
                pomProperty    : 'cors.module.version',
                updatePom      : true,
                updateWhitelist: false,
        ],
        MuleCE : [
                pomPath        : '.',
                pomProperty    : 'mule.version',
                updatePom      : true,
                updateWhitelist: false,
        ],
        RamlParser : [
                pomPath        : '.',
                pomProperty    : 'ramlParser.version',
                updatePom      : true,
                updateWhitelist: false,
        ],
        RamlParser2 : [
                pomPath        : '.',
                pomProperty    : 'ramlParser2.version',
                updatePom      : true,
                updateWhitelist: false,
        ],

]

def cliBuilder = new CliBuilder()
cliBuilder.with {
    h(longOpt: "help", "show usage info")
    d(longOpt: "dependency", args: 1, argName: 'dependency', required: true, "Dependency to update")
    t(longOpt: "to", args: 1, argName: 'version_to', required: false, "Change dependencies to selected version (e.g. 3.6.1-SNAPSHOT)")
    p(longOpt: "project", required: false, 'Change dependencies to current Maven module version')
}

def usage() {
    def dependencyNameList = "";
    dependencies.each { dependencyNameList += it.key + " | " }

    println "Usage:"
    println ""
    println "Updates MuleSoft dependencies versions in pom.xml and assembly-whitelist.txt"
    println ""
    println "Supported dependencies are:"
    println ""
    println "[ ${dependencyNameList[0..-4]} ]"
    println ""
    println "Syntax:"
    println "groovy UpdateMuleSoftDependencies.groovy -d <dependency> -p"
    println "groovy UpdateMuleSoftDependencies.groovy -d <dependency> -t <version_to>"
    println ""
    println "Examples:"
    println "groovy UpdateMuleSoftDependencies.groovy -d ARM -t 3.5.1-SNAPSHOT"
    println "groovy UpdateMuleSoftDependencies.groovy -d DataWeave -p"
    println ""
    System.exit(1)
}

parseOptions(cliBuilder)
executeScript(root)

void parseOptions(cliBuilder) {
    def options = cliBuilder.parse(args)
    if (!options) {
        System.exit(1)
    }
    if (options.h) {
        cliBuilder.usage()
        usage()
        System.exit(0)
    }

    validateArgsByMode(options)

    dependencyName = options.d

    if (dependencyName in dependencies) {
        dependency = dependencies[dependencyName]
    } else {
        println "\n ERROR: the '${dependencyName}' dependency was not found in the dependency list included in the script. \n"
        usage()
        System.exit(1)
    }

    root = new File((dependency['pomPath']) ? dependency['pomPath'] : assemblyWhitelistPath)

    if (options.p) {
        dependenciesVersionTo = pomProjectVersion
        whitelistDependenciesVersionTo = whitelistProductVersion
    } else {
        dependenciesVersionTo = options.t
        whitelistDependenciesVersionTo = options.t
    }
}

void executeScript(root) {
    if (dependency['updatePom']) {
        dependenciesVersionFrom = getDependenciesVersionInPomXML(root)
        updatePomDependenciesVersionProperty()
    }
    if (dependency['updateWhitelist']) {
        dependenciesVersionFrom = getDependenciesVersionInWhitelist(assemblyWhitelistPath)
        if (dependenciesVersionFrom.equals(pomProjectVersion)) {
            whitelistDependenciesVersionFrom = whitelistProductVersion
        } else {
            whitelistDependenciesVersionFrom = dependenciesVersionFrom
        }
        updateAssemblyWhitelistEntries()
    }
}

void updateAssemblyWhitelistEntries() {
    whitelist = new File(assemblyWhitelistPath, whitelistName)

    def filePrefixList = (dependency['filePrefix'] in List) ? dependency['filePrefix'] : [dependency['filePrefix']]

    for (def filePrefix : filePrefixList) {
        updateVersion(whitelist, filePrefix, dependency['fileSuffix'], whitelistDependenciesVersionFrom, whitelistDependenciesVersionTo, true)
    }
}

void updatePomDependenciesVersionProperty() {
    updateVersion(new File(root, pomName), '<' + dependency['pomProperty'] + '>', '</' + dependency['pomProperty'] + '>', dependenciesVersionFrom, dependenciesVersionTo, false)

    if (dependency['pomPathTests']) {
        updateVersion(new File(dependency['pomPathTests'], pomName), '<' + dependency['pomProperty'] + '>', '</' + dependency['pomProperty'] + '>', dependenciesVersionFrom, dependenciesVersionTo, false)
    }
}

void updateVersion(File file, String prefix, String suffix, String oldVersion, String newVersion, Boolean isAssemblyWhitelist) {
    println("Processing  ${file}")
    updateFile(file) { updateSurroundedText(it, prefix, suffix, oldVersion, newVersion, isAssemblyWhitelist) }
}

def static updateFile(file, Closure updateText) {
    if (!file.exists()) {
        throw new UpdaterException('File not found: ${file}')
    }
    file.write(updateText(file.getText()))
}

def static updateSurroundedText(String text, String prefix, String suffix, String old, String newOne, Boolean isAssemblyWhitelist) {

    String oldText = ""
    String newText = ""

    if(isAssemblyWhitelist) {
        // In the case that the version ends with -+ I don't have to use the sufix (.jar)
        oldText = old.endsWith("-+") ? prefix + old : prefix + old + suffix
        newText = newOne.endsWith("-+") ? prefix + newOne : prefix + newOne + suffix
    }else{
        // In the case that the version ends with -+ I have to change it to SNAPSHOT in the pom.xml.
        // The old version can't have the wildcard "+" in the pom.
        oldText = prefix + old + suffix
        newText = newOne.endsWith("-+") ? prefix + newOne.replace("+","SNAPSHOT") + suffix : prefix + newOne + suffix
    }

    if (!text.contains(oldText)) {
        throw new UpdaterException("Couldn't find [${oldText}] in text.")
    };
    println("Replacing [${oldText}] with [${newText}]\n")
    return text.replace(oldText, newText);
}

def getDependenciesVersionInPomXML(pomPath) {
    def pomXML = new XmlSlurper().parse(new File("$pomPath/$pomName"))
    def pomProperty = dependency['pomProperty']
    pomXML.properties."$pomProperty".toString()
}

def getDependenciesVersionInWhitelist(root) {

    def version

    def firstFilePrefixInList = (dependency['filePrefix'] in List) ? dependency['filePrefix'].first() : dependency['filePrefix']

    // Added this regex check to avoid get the wrong version when there are lines that have the same substring.
    // For example using the prefix 'raml-parser-' and the lines 'raml-parser-2-1.0.0-+' and 'raml-parser-0.9.1' before the regex validation it obtains the version '-2-1.0.0'
    def regexNumberVersion = ~/^\d+\.|.productVersion./

    def whitelistFile = new File(root, whitelistName)

    whitelistFile.eachLine
            {
                if (!version && it.contains(firstFilePrefixInList) && it.minus(firstFilePrefixInList) =~ regexNumberVersion) {
                    version = it.minus(firstFilePrefixInList).minus(dependency['fileSuffix'])
                }
            }
    if (version != null) {
        return version
    }

    def absolutePathWhitelist = whitelistFile.getAbsolutePath()
    throw new RuntimeException("Prefix '${firstFilePrefixInList}' not found in ${absolutePathWhitelist}")
}


def validateArgsByMode(options) {
    if (!options.p && !options.t) {
        println "\n ERROR: You need to specify either '-p' or '-t <version_to>' arguments. \n"
        usage()
        System.exit(1)
    }
    if (options.p && options.t) {
        println "\n ERROR: You cannot specify both '-p' and '-t' arguments at the same time. \n"
        usage()
        System.exit(1)
    }
}


class UpdaterException extends RuntimeException {

    public UpdaterException(String message) {
        super(message)
    }
}