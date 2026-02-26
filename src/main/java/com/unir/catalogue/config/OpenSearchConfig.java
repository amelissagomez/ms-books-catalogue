package com.unir.catalogue.config;

import org.opensearch.client.RestHighLevelClient;
import org.opensearch.data.client.orhlc.AbstractOpenSearchConfiguration;
import org.opensearch.data.client.orhlc.ClientConfiguration;
import org.opensearch.data.client.orhlc.RestClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.unir.catalogue.search")
public class OpenSearchConfig extends AbstractOpenSearchConfiguration {


	@Value("${opensearch.credentials.user:}")
	private String username;

	@Value("${opensearch.credentials.password:}")
	private String password;

	@Value("${opensearch.host}")
	private String clusterEndpoint;


	@Override
	public RestHighLevelClient opensearchClient() {
		final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
				.connectedTo(clusterEndpoint + ":443")
				.usingSsl()
				.withBasicAuth(username, password)
				.build();
		return RestClients.create(clientConfiguration).rest();
	}
}


