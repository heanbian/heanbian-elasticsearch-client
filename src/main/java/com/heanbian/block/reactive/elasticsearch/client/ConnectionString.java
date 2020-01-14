package com.heanbian.block.reactive.elasticsearch.client;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConnectionString {

	private static final String ELASTICSEARCH_PREFIX = "elasticsearch://";
	private String connectionString;
	private String username;
	private String password;
	private List<String> hosts;

	public ConnectionString(final String connectionString) {
		this.connectionString = connectionString;
		boolean isElasticsearchProtocol = connectionString.startsWith(ELASTICSEARCH_PREFIX);
		if (!isElasticsearchProtocol) {
			throw new IllegalArgumentException(
					format("The connection string is invalid. Connection strings must start with either '%s'",
							ELASTICSEARCH_PREFIX));
		}

		String unprocessedConnectionString = connectionString.substring(ELASTICSEARCH_PREFIX.length());

		String userInfo;
		String hostIdentifier;
		int idx = unprocessedConnectionString.lastIndexOf("@");
		if (idx > 0) {
			userInfo = unprocessedConnectionString.substring(0, idx).replace("+", "%2B");
			hostIdentifier = unprocessedConnectionString.substring(idx + 1);
			int colonCount = countOccurrences(userInfo, ":");
			if (userInfo.contains("@") || colonCount > 1) {
				throw new IllegalArgumentException("The connection string contains invalid user information. "
						+ "If the username or password contains a colon (:) or an at-sign (@) then it must be urlencoded");
			}
			if (colonCount == 0) {
				this.username = urldecode(userInfo);
			} else {
				idx = userInfo.indexOf(":");
				this.username = urldecode(userInfo.substring(0, idx));
				this.password = urldecode(userInfo.substring(idx + 1), true);
			}
		} else {
			hostIdentifier = unprocessedConnectionString;
		}

		this.hosts = unmodifiableList(parseHosts(asList(hostIdentifier.split(","))));
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public List<String> getHosts() {
		return hosts;
	}

	public String getConnectionString() {
		return connectionString;
	}

	private List<String> parseHosts(final List<String> rawHosts) {
		if (rawHosts.size() == 0) {
			throw new IllegalArgumentException("The connection string must contain at least one host");
		}
		List<String> hosts = new ArrayList<String>();
		for (String host : rawHosts) {
			if (host.length() == 0) {
				throw new IllegalArgumentException(
						format("The connection string contains an empty host '%s'. ", rawHosts));
			} else {
				int colonCount = countOccurrences(host, ":");
				if (colonCount > 1) {
					throw new IllegalArgumentException(format(
							"The connection string contains an invalid host '%s'. "
									+ "Reserved characters such as ':' must be escaped according RFC 2396. "
									+ "Any IPv6 address literal must be enclosed in '[' and ']' according to RFC 2732.",
							host));
				} else if (colonCount == 1) {
					validatePort(host, host.substring(host.indexOf(":") + 1));
				}
			}
			hosts.add(host);
		}
		Collections.sort(hosts);
		return hosts;
	}

	private void validatePort(final String host, final String port) {
		boolean invalidPort = false;
		try {
			int portInt = Integer.parseInt(port);
			if (portInt <= 0 || portInt > 65535) {
				invalidPort = true;
			}
		} catch (NumberFormatException e) {
			invalidPort = true;
		}
		if (invalidPort) {
			throw new IllegalArgumentException(format("The connection string contains an invalid host '%s'. "
					+ "The port '%s' is not a valid, it must be an integer between 0 and 65535", host, port));
		}
	}

	private int countOccurrences(final String haystack, final String needle) {
		return haystack.length() - haystack.replace(needle, "").length();
	}

	private String urldecode(final String input) {
		return urldecode(input, false);
	}

	private String urldecode(final String input, final boolean password) {
		try {
			return URLDecoder.decode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			if (password) {
				throw new IllegalArgumentException(
						"The connection string contained unsupported characters in the password.");
			} else {
				throw new IllegalArgumentException(
						format("The connection string contained unsupported characters: '%s'."
								+ "Decoding produced the following error: %s", input, e.getMessage()));
			}
		}
	}
}