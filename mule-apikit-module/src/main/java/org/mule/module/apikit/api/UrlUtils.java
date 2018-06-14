/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api;

import org.apache.commons.lang3.StringUtils;

import java.net.URL;


//import org.mule.runtime.api.message.Message;
//import org.mule.runtime.core.api.InternalEvent;
//import org.mule.runtime.core.util.StringUtils;

public class UrlUtils {

  public static final String HTTP_CONTEXT_PATH_PROPERTY = "http.context.path";
  public static final String HTTP_REQUEST_PATH_PROPERTY = "http.request.path";
  private static final String BIND_TO_ALL_INTERFACES = "0.0.0.0";
  public static final String FULL_DOMAIN = "fullDomain";
  private static final String HTTP = "http://";
  private static final String HTTPS = "https://";


  private UrlUtils() {}

  //public static String getBaseSchemeHostPort(Event event) {
  //  String host = ((HttpRequestAttributes) event.getMessage().getAttributes()).getHeaders().get("host");
  //  String chHost = System.getProperty("fullDomain");
  //  if (chHost != null) {
  //    host = chHost;
  //  }
  //  return getScheme(event.getMessage()) + "://" + host;
  //}
  //
  //public static String getScheme(Message message) {
  //  String scheme = ((HttpRequestAttributes) message.getAttributes()).getScheme();
  //  if (scheme == null) {
  //    String endpoint = ((HttpRequestAttributes) message.getAttributes()).getRequestUri(); //TODO CHECK IF THIS IS THE CORRECT PROPERTY//.getInboundProperty("http.context.uri");
  //    if (endpoint == null) {
  //      throw new ApikitRuntimeException("Cannot figure out the request scheme");
  //    }
  //    if (endpoint.startsWith("http:")) {
  //      scheme = "http";
  //    } else if (endpoint.startsWith("https:")) {
  //      scheme = "https";
  //    } else {
  //      throw new ApikitRuntimeException("Unsupported scheme: " + endpoint);
  //    }
  //  }
  //  return scheme;
  //}

  //public static String getBaseSchemeHostPort(String baseUri) {
  //  URL url;
  //  try {
  //    url = new URL(baseUri);
  //  } catch (MalformedURLException e) {
  //    return "http://localhost";
  //  }
  //  return url.getProtocol() + "://" + url.getAuthority();
  //}

  //public static String getResourceRelativePath(Message message) {
  //  String path = ((HttpRequestAttributes) message.getAttributes()).getRequestPath();
  //  //String basePath = getBasePath(message);
  //  //path = path.substring(basePath.length());
  //  if (!path.startsWith("/") && !path.isEmpty()) {
  //    path = "/" + path;
  //  }
  //  return path;
  //}

  private static int getEndOfBasePathIndex(String baseAndApiPath, String requestPath) {
    int amountOfSlashesInBasePath = 0;
    for (int i = 0; i < baseAndApiPath.length(); i++) {
      if (Character.compare(baseAndApiPath.charAt(i), '/') == 0) {
        amountOfSlashesInBasePath++;
      }
    }
    int amountOfSlashesInRequestPath = 0;
    int character = 0;
    for (; character < requestPath.length() && amountOfSlashesInRequestPath < amountOfSlashesInBasePath; character++) {
      if (Character.compare(requestPath.charAt(character), '/') == 0) {
        amountOfSlashesInRequestPath++;
      }
    }

    return character;
  }

  public static String getRelativePath(String baseAndApiPath, String requestPath) {
    int character = getEndOfBasePathIndex(baseAndApiPath, requestPath);
    String relativePath = requestPath.substring(character);
    if (!"".equals(relativePath)) {
      for (; character > 0 && Character.compare(requestPath.charAt(character - 1), '/') == 0; character--) {
        relativePath = "/" + relativePath;
      }
    } else {
      relativePath += "/";
    }

    return relativePath;
  }

  public static String getListenerPath(String listenerPath, String requestPath) {

    if (!listenerPath.startsWith("/")) {
      listenerPath = "/" + listenerPath;
    }
    if (!requestPath.startsWith("/")) {
      requestPath = "/" + requestPath;
    }
    int slashesAmount = 0;
    for (int i = 0; i < listenerPath.length(); i++) {
      if (listenerPath.charAt(i) == '/') {
        slashesAmount++;
      }
    }
    String[] split = requestPath.split("/");
    String result = "";
    if (split.length == 0) {
      return "/";
    }
    if (split.length == 1 && split[0].equals("")) {
      return "/";
    }
    for (int i = 0; i < slashesAmount; i++) {
      if (!split[i].equals("")) {
        result += "/" + split[i];
      }
    }
    return result;
  }


  public static String getBasePath(String baseAndApiPath, String requestPath) {
    int character = getEndOfBasePathIndex(baseAndApiPath, requestPath);
    return requestPath.substring(0, character);
  }
  //
  //public static String getQueryString(Message message) {
  //  String queryString = ((HttpRequestAttributes) message.getAttributes()).getQueryString();
  //  return queryString == null ? "" : queryString;
  //}

  //public static String rewriteBaseUri(String raml, String baseSchemeHostPort) {
  //  return replaceBaseUri(raml, "https?://[^/]*", baseSchemeHostPort);
  //}

  public static String replaceBaseUri(String raml, String newBaseUri) {
    if (newBaseUri != null) {
      return replaceBaseUri(raml, ".*$", newBaseUri);
    }
    return raml;
  }

  private static String replaceBaseUri(String raml, String regex, String replacement) {
    String[] split = raml.split("\n");
    boolean found = false;
    for (int i = 0; i < split.length; i++) {
      if (split[i].startsWith("baseUri: ")) {
        found = true;
        split[i] = split[i].replaceFirst(regex, replacement);
        if (!split[i].contains("baseUri: ")) {
          split[i] = "baseUri: " + split[i];
        }
      }
    }
    if (!found) {
      for (int i = 0; i < split.length; i++) {
        if (split[i].startsWith("title:")) {
          if (replacement.contains("baseUri:")) {
            split[i] = split[i] + "\n" + replacement;
          } else {
            split[i] = split[i] + "\n" + "baseUri: " + replacement;
          }
        }
      }
    }
    return StringUtils.join(split, "\n");
  }

  public static String getCompletePathFromBasePathAndPath(String basePath, String listenerPath) {

    String path = basePath + listenerPath;
    if (path.contains("/*")) {
      path = path.replace("/*", "");
    }
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    if (path.contains("//")) {
      path = path.replace("//", "/");
    }
    return path;
  }

  /**
   * Creates URL where the server must redirect the client
   * @return The redirect URL
   */
  public static String getRedirectLocation(String scheme, String remoteAddress, String requestPath, String queryString) {
    String redirectLocation = scheme + "://" + remoteAddress + requestPath + "/";

    if (StringUtils.isNotEmpty(queryString)) {
      redirectLocation += "?" + queryString;
    }

    return redirectLocation;
  }

  public static String getBaseUriReplacement(String apiServer) {
    if (apiServer == null) {
      return null;
    }

    String baseUriReplacement = apiServer;
    if (apiServer.contains(BIND_TO_ALL_INTERFACES)) {
      String fullDomain = System.getProperty(FULL_DOMAIN);
      if (fullDomain != null) {
        URL url = null;
        try {
          url = new URL(apiServer);
        } catch (Exception e) {
          return apiServer;
        }
        String path = url.getPath();
        if (fullDomain.endsWith("/") && path.length() > 0 && path.startsWith("/")) {
          path = path.length() > 1 ? path.substring(1) : "";
        } else if (!fullDomain.endsWith("/") && path.length() > 0 && !path.startsWith("/")) {
          fullDomain += "/";
        }
        if (fullDomain.contains("://")) {
          baseUriReplacement = fullDomain + path;
        } else {
          final String protocol = apiServer.contains(HTTPS) ? HTTPS : HTTP;
          baseUriReplacement = protocol + fullDomain + path;
        }
      } else {
        baseUriReplacement = baseUriReplacement.replace(BIND_TO_ALL_INTERFACES, "localhost");
      }
    }
    return baseUriReplacement;
  }


}
