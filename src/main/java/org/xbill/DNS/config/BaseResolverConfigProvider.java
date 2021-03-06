// SPDX-License-Identifier: BSD-2-Clause
package org.xbill.DNS.config;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

/**
 * Base class for resolver config providers that provides a default implementation for the lists and
 * utility methods to prevent duplicates.
 */
abstract class BaseResolverConfigProvider implements ResolverConfigProvider {
  final Logger log = LoggerFactory.getLogger(getClass());
  List<InetSocketAddress> nameservers = new ArrayList<>(3);
  List<Name> searchlist = new ArrayList<>(1);

  void parseSearchPathList(String search, String delimiter) {
    if (search != null) {
      StringTokenizer st = new StringTokenizer(search, delimiter);
      while (st.hasMoreTokens()) {
        addSearchPath(st.nextToken());
      }
    }
  }

  void addSearchPath(String searchPath) {
    if (searchPath == null || searchPath.isEmpty()) {
      return;
    }

    try {
      Name n = Name.fromString(searchPath, Name.root);
      if (!searchlist.contains(n)) {
        searchlist.add(n);
        log.debug("Added {} to search paths", n);
      }
    } catch (TextParseException e) {
      log.warn("Could not parse search path {} as a dns name, ignoring", searchPath);
    }
  }

  void addNameserver(InetSocketAddress server) {
    if (!nameservers.contains(server)) {
      nameservers.add(server);
      log.debug("Added {} to nameservers", server);
    }
  }

  int parseNdots(String token) {
    if (token != null && !token.isEmpty()) {
      try {
        int ndots = Integer.parseInt(token);
        if (ndots >= 0) {
          if (ndots > 15) {
            // man resolv.conf:
            // The value for this option is silently capped to 15
            ndots = 15;
          }

          return ndots;
        }
      } catch (NumberFormatException e) {
        // ignore
      }
    }

    return 1;
  }

  @Override
  public final List<InetSocketAddress> servers() {
    return Collections.unmodifiableList(nameservers);
  }

  @Override
  public final List<Name> searchPaths() {
    return Collections.unmodifiableList(searchlist);
  }
}
