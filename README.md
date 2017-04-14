APIKit
======

APIKit is a toolkit that facilitates REST development. APIKit features include the ability to take a REST API designed in RAML, automatically generate backend implementation flows for it, and then run and test the API with a pre-packaged console.

See http://raml.org for more information about RAML.

Contribute
==========
APIKit is open source and we love contributions! If you have an idea for a great improvement or spy an issue you’re keen to fix, you can fork us on [github](https://github.com/mulesoft/apikit).

No contribution is too small – providing feedback, [reporting issues](http://www.mulesoft.org/jira/browse/APIKIT) and participating in the [community forums](http://forum.mulesoft.org/mulesoft) is invaluable and extremely helpful for all our users. Please refer to the following guidelines for details.

# Setting up the development environment

## Getting the Source Code

APIKit source code lives on Github. Complete the following procedure to locate the code and get it onto your local drive.

If you're new to Git, consider reading [Pro Git](http://git-scm.com/book) to absorb the basics.
 Just want a Read-Only version of APIKit source code?

1. [Create](https://help.github.com/articles/signing-up-for-a-new-github-account) or log in to your github account. 
2. If you haven't already done so, [set up git](https://help.github.com/articles/set-up-git) on your local drive. 
3. Navigate to APIKit's github page at: [https://github.com/mulesoft/apikit.git](https://github.com/mulesoft/apikit.git) 
4. Click the Fork button at the top right corner of the page, then select your own git repository into which github inserts a copy of the repository.
5. Prepare to clone your forked APIKit repository from your github account to your local drive via a secure file transfer connection. As per git's recommendation, we advise using HTTPS to transfer the source code files to your local drive. However, if you prefer to establish a secure connection for transferring the files via SSH, follow git's procedure to [generate SSH keys](https://help.github.com/articles/generating-ssh-keys).
6. In the command line, create or navigate to an existing folder on your local drive into which you wish to store your forked clone of APIKit source code.
7. From the command line, execute one of the following:
    - For **HTTPS**:  `git clone https://github.com/<yourreponame>/apikit`
    - For **SSH**:  `git clone git@github.com:<username>/<repo-name>.git`
8. Add the upstream repository so that you can pull changes and stay updated with changes to the apikit-3.8.x (i.e. master) branch. From the command line, execute one of the following:
    - For **HTTPS**: `git remote add upstream https://github.com/mulesoft/apikit.git`
    - For **SSH**: `git remote add upstream git@github.com:mulesoft/apikit.git`
    
We are ready to develop our improvements.

#  Developing your contribution

Working directly on the master version of APIKit source code would likely result in merge conflicts with the original master. Instead, as a best practice for contributing to source code, work on your project in a feature branch.

## Creating your feature branch

In order to create our feature branch we should follow these steps:

1. From your local drive, create a new branch in which you can work on your bug fix or improvement using the following command:
`git branch yourJIRAissuenumber`.
2. Switch to the new branch using the following command: 
`git checkout yourJIRAissuenumber`.

Now we should be able to make our very first compilation of the APIKit source code. We just need to instruct Maven to download all the dependent libraries and compile the project, you can do so execution the following command Within the directory into which you cloned the APIKit source code: `mvn -DskipTests install`.

Note that if this is your first time using Maven, the download make take some minutes to complete.

Now that you're all set with a local development environment and your own branch of APIKit source code, you're ready get kicking! The following steps briefly outline the development lifecycle to follow to develop and commit your changes in preparation for submission.

1. If you are using an IDE, make sure you read the previous section about [IDE configuration](#configuring-the-ide).
2. Review the [Mule Coding Style](https://github.com/mulesoft/mule/blob/mule-3.x/STYLE.md) documentation to ensure you adhere to source code standards, thus increasing the likelihood that your changes will be merged with the `apikit-3.8.x` (i.e. master) source code.
3. Import the APIKit source code project into your IDE (if you are using one), then work on your changes, fixes or improvements. 
4. Debug and test your  local version, resolving any issues that arise. 
5. Save your changes locally.
6. Prepare your changes for a Pull Request by first squashing your changes into a single commit on your branch using the following command: 
`git rebase -i apikit-3.8.x`.
7. Push your squashed commit to your branch on your github repository. Refer to [Git's documentation](http://git-scm.com/book/en/v2/Git-Basics-Recording-Changes-to-the-Repository) for details on how to commit your changes.
8. Regularly update your branch with any changes or fixes applied to the apikit-3.8.x branch. Refer to details below.

## Updating Your feature Branch

To ensure that your cloned version of APIKit source code remains up-to-date with any changes to the apikit-3.8.x (i.e. master) branch, regularly update your branch to rebase off the latest version of the master.  

1. Pull the latest changes from the "upstream" master apikit-3.8.x branch using the following commands:

```shell
git fetch upstream
git fetch upstream --tags 
```
2. Ensure you are working with the master branch using the following command:

```shell
git checkout apikit-3.8.x
```
3. Merge the latest changes and updates from the master branch to your feature branch using the following command:

```shell
git merge upstream/apikit-3.8.x
```
4. Push any changes to the master to your forked clone using the following commands:

```shell
git push origin apikit-3.8.x
git push origin --tags
```
5. Access your feature branch once again (to continue coding) using the following command:

```shell
git checkout dev/yourreponame/bug/yourJIRAissuenumber
```
6. Rebase your branch from the latest version of the master branch using the following command:

```shell
git rebase apikit-3.8.x
```
7. Resolve any conflicts on your feature branch that may appear as a result of the changes to apikit-3.8.x (i.e. master).
8. Push the newly-rebased branch back to your fork on your git repository using the following command:

```shell
git push origin dev/yourreponame/bug/yourJIRAissuenumber -f
```

##  Submitting a Pull Request

Ready to submit your patch for review and merging? Initiate a pull request in github!

1. Review the [MuleSoft Contributor's Agreement](http://www.mulesoft.org/legal/contributor-agreement.html). Before any contribution is accepted, we need you to run the following notebook [script ](https://api-notebook.anypoint.mulesoft.com/notebooks#bc1cf75a0284268407e4). This script will ask you to login to github and accept our Contributor's Agreement. That process creates an issue in our contributors project with your name.
2. From the repo of your branch, click the Pull Request button.
3. In the Pull Request Preview dialog, enter a title and optional description of your changes, review the commits that form part of your pull request, then click Send Pull Request (Refer to github's [detailed instructions](https://help.github.com/articles/using-pull-requests) for submitting a pull request).
4. APIKit's core dev team reviews the pull request and may initiate discussion or ask questions about your changes in a Pull Request Discussion. The team can then merge your commits with the master where appropriate. We will validate acceptance of the agreement at this step. 
5. If you have made changes or corrections to your commit after having submitted the pull request, go back to the Pull Request page and update the Commit Range (via the Commits tab), rather than submitting a new pull request. 

