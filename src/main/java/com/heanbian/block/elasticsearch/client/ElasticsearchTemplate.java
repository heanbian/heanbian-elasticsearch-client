package com.heanbian.block.elasticsearch.client;

import static org.elasticsearch.client.RequestOptions.DEFAULT;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.explain.ExplainRequest;
import org.elasticsearch.action.explain.ExplainResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Cancellable;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.DeleteAliasRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.SortBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.heanbian.block.elasticsearch.client.executor.DefaultExecutorImpl;
import com.heanbian.block.elasticsearch.client.executor.Executor;
import com.heanbian.block.elasticsearch.client.operator.Operator;
import com.heanbian.block.elasticsearch.client.page.Page;

/**
 * 
 * @author Heanbian
 *
 */
public class ElasticsearchTemplate {
	private final static String DATE_TIME_FORMATTER = "yyyy-MM-dd HH:mm:ss";

	private final Executor executor = new DefaultExecutorImpl(5);
	private final AliasesOperator aliasesOperator = new AliasesOperator();
	private final BulkOperator bulkOperator = new BulkOperator();
	private final BulkAsyncOperator bulkAsyncOperator = new BulkAsyncOperator();
	private final CreateIndexOperator createIndexOperator = new CreateIndexOperator();
	private final ClearScrollOperator clearScrollOperator = new ClearScrollOperator();
	private final CountRequestOperator countRequestOperator = new CountRequestOperator();
	private final DeleteIndexOperator deleteIndexOperator = new DeleteIndexOperator();
	private final DeleteByQueryRequestOperator deleteByQueryRequestOperator = new DeleteByQueryRequestOperator();
	private final DeleteAliasOperator deleteAliasOperator = new DeleteAliasOperator();
	private final ExplainOperator explainOperator = new ExplainOperator();
	private final ExistsRequestOperator existsRequestOperator = new ExistsRequestOperator();
	private final GetOperator operator = new GetOperator();
	private final IndicesExistsOperator indicesExistsOperator = new IndicesExistsOperator();
	private final SearchOperator searchOperator = new SearchOperator();
	private final SearchScrollOperator searchScrollOperator = new SearchScrollOperator();
	private final UpdateRequestOperator updateRequestOperator = new UpdateRequestOperator();
	private final UpdateByQueryRequestOperator updateByQueryRequestOperator = new UpdateByQueryRequestOperator();

	private final RestHighLevelClient client;
	private final ObjectMapper mapper;

	public ElasticsearchTemplate(String connectionString) {
		this(new ConnectionString(connectionString).getRestHighLevelClient(), defaultObjectMapper());
	}

	public ElasticsearchTemplate(RestHighLevelClient client, ObjectMapper mapper) {
		this.client = client;
		this.mapper = mapper;
	}

	private static ObjectMapper defaultObjectMapper() {
		DateTimeFormatter f = DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER);

		JavaTimeModule module = new JavaTimeModule();
		module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(f));

		ObjectMapper om = new ObjectMapper();
		om.registerModules(module);
		om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		return om;
	}

	public <R, S> S exec(Operator<R, S> operator, R request) {
		return executor.exec(operator, request);
	}

	public class CountRequestOperator implements Operator<CountRequest, CountResponse> {

		@Override
		public CountResponse operator(CountRequest request) throws IOException {
			return client.count(request, DEFAULT);
		}
	}

	public class UpdateRequestOperator implements Operator<UpdateRequest, UpdateResponse> {

		@Override
		public UpdateResponse operator(UpdateRequest request) throws IOException {
			return client.update(request, DEFAULT);
		}
	}

	public class ExistsRequestOperator implements Operator<GetRequest, Boolean> {

		@Override
		public Boolean operator(GetRequest request) throws IOException {
			return client.exists(request, DEFAULT);
		}
	}

	public class DeleteByQueryRequestOperator implements Operator<DeleteByQueryRequest, BulkByScrollResponse> {

		@Override
		public BulkByScrollResponse operator(DeleteByQueryRequest request) throws IOException {
			return client.deleteByQuery(request, DEFAULT);
		}
	}

	public class UpdateByQueryRequestOperator implements Operator<UpdateByQueryRequest, BulkByScrollResponse> {

		@Override
		public BulkByScrollResponse operator(UpdateByQueryRequest request) throws IOException {
			return client.updateByQuery(request, DEFAULT);
		}
	}

	public class CreateIndexOperator implements Operator<CreateIndexRequest, CreateIndexResponse> {

		@Override
		public CreateIndexResponse operator(CreateIndexRequest request) throws IOException {
			return client.indices().create(request, DEFAULT);
		}
	}

	public class DeleteIndexOperator implements Operator<DeleteIndexRequest, AcknowledgedResponse> {

		@Override
		public AcknowledgedResponse operator(DeleteIndexRequest request) throws IOException {
			return client.indices().delete(request, DEFAULT);
		}
	}

	public class DeleteAliasOperator
			implements Operator<DeleteAliasRequest, org.elasticsearch.client.core.AcknowledgedResponse> {

		@Override
		public org.elasticsearch.client.core.AcknowledgedResponse operator(DeleteAliasRequest request)
				throws IOException {
			return client.indices().deleteAlias(request, DEFAULT);
		}
	}

	public class BulkOperator implements Operator<BulkRequest, BulkResponse> {

		@Override
		public BulkResponse operator(BulkRequest request) throws IOException {
			return client.bulk(request, DEFAULT);
		}
	}

	public class BulkAsyncOperator implements Operator<BulkRequest, Cancellable> {

		@Override
		public Cancellable operator(BulkRequest request) throws IOException {
			return client.bulkAsync(request, DEFAULT, new ActionListener<BulkResponse>() {

				@Override
				public void onResponse(BulkResponse response) {// ignore
				}

				@Override
				public void onFailure(Exception e) {// ignore
				}
			});
		}
	}

	public class ExplainOperator implements Operator<ExplainRequest, ExplainResponse> {

		@Override
		public ExplainResponse operator(ExplainRequest request) throws IOException {
			return client.explain(request, DEFAULT);
		}
	}

	public class GetOperator implements Operator<GetRequest, GetResponse> {

		@Override
		public GetResponse operator(GetRequest request) throws IOException {
			return client.get(request, DEFAULT);
		}
	}

	public class SearchOperator implements Operator<SearchRequest, SearchResponse> {

		@Override
		public SearchResponse operator(SearchRequest request) throws IOException {
			return client.search(request, DEFAULT);
		}
	}

	public class SearchScrollOperator implements Operator<SearchScrollRequest, SearchResponse> {

		@Override
		public SearchResponse operator(SearchScrollRequest request) throws IOException {
			return client.scroll(request, DEFAULT);
		}
	}

	public class ClearScrollOperator implements Operator<ClearScrollRequest, ClearScrollResponse> {

		@Override
		public ClearScrollResponse operator(ClearScrollRequest request) throws IOException {
			return client.clearScroll(request, DEFAULT);
		}
	}

	public class IndicesExistsOperator implements Operator<GetIndexRequest, Boolean> {

		@Override
		public Boolean operator(GetIndexRequest request) throws IOException {
			return client.indices().exists(request, DEFAULT);
		}
	}

	public class AliasesOperator implements Operator<GetAliasesRequest, GetAliasesResponse> {

		@Override
		public GetAliasesResponse operator(GetAliasesRequest request) throws IOException {
			return client.indices().getAlias(request, DEFAULT);
		}
	}

	public Set<String> getAliases() {
		return exec(aliasesOperator, new GetAliasesRequest()).getAliases().keySet();
	}

	public boolean indicesExists(String... indices) {
		return exec(indicesExistsOperator, new GetIndexRequest(indices));
	}

	public CreateIndexResponse createIndex(String index, int shards, int replicas) {
		return createIndex(index, shards, replicas, null, null);
	}

	public CreateIndexResponse createIndex(String index, int shards, int replicas, Map<String, ?> mapping) {
		return createIndex(index, shards, replicas, mapping, null);
	}

	public CreateIndexResponse createIndex(String index, int shards, int replicas, Map<String, ?> mapping,
			Map<String, ?> alias) {
		CreateIndexRequest request = new CreateIndexRequest(index);
		request.settings(
				Settings.builder().put("index.number_of_shards", shards).put("index.number_of_replicas", replicas));
		if (mapping != null) {
			request.mapping(mapping);
		}
		if (alias != null) {
			request.aliases(alias);
		}
		return exec(createIndexOperator, request);
	}

	public <T extends ElasticsearchId> BulkResponse bulkInsert(String index, T source) {
		return bulkInsert(index, List.of(source));
	}

	public <T extends ElasticsearchId> BulkResponse bulkInsert(String index, List<T> sources) {
		BulkRequest request = new BulkRequest();
		sources.forEach(d -> {
			try {
				byte[] json = mapper.writeValueAsBytes(d);
				request.add(new IndexRequest(index).id(esId(d)).source(json, XContentType.JSON));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		});
		return exec(bulkOperator, request);
	}

	public <T extends ElasticsearchId> Cancellable bulkInsertAsync(String index, T source) {
		return bulkInsertAsync(index, List.of(source));
	}

	public <T extends ElasticsearchId> Cancellable bulkInsertAsync(String index, List<T> sources) {
		BulkRequest request = new BulkRequest();
		sources.forEach(d -> {
			try {
				byte[] json = mapper.writeValueAsBytes(d);
				request.add(new IndexRequest(index).id(esId(d)).source(json, XContentType.JSON));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		});
		return exec(bulkAsyncOperator, request);
	}

	public BulkResponse bulkDelete(String index, String... ids) {
		return bulkDelete(index, List.of(ids));
	}

	public BulkResponse bulkDelete(String index, List<String> ids) {
		BulkRequest request = new BulkRequest();
		ids.forEach(id -> {
			request.add(new DeleteRequest(index, id));
		});
		return exec(bulkOperator, request);
	}

	public Cancellable bulkDeleteAsync(String index, String... ids) {
		return bulkDeleteAsync(index, List.of(ids));
	}

	public Cancellable bulkDeleteAsync(String index, List<String> ids) {
		BulkRequest request = new BulkRequest();
		ids.forEach(id -> {
			request.add(new DeleteRequest(index, id));
		});
		return exec(bulkAsyncOperator, request);
	}

	public GetResponse findById(String index, String id) {
		return exec(operator, new GetRequest(index, id));
	}

	public <T extends ElasticsearchId> T findById(String index, String id, Class<T> clazz) {
		GetResponse response = findById(index, id);
		try {
			if (response == null || org.elasticsearch.common.Strings.isNullOrEmpty(response.getSourceAsString())) {
				return null;
			}
			return mapper.readValue(response.getSourceAsString(), clazz);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public <T extends ElasticsearchId> BulkResponse bulkUpdate(String index, T source) {
		return bulkUpdate(index, List.of(source));
	}

	public <T extends ElasticsearchId> BulkResponse bulkUpdate(String index, List<T> sources) {
		BulkRequest request = new BulkRequest();
		sources.forEach(d -> {
			try {
				byte[] json = mapper.writeValueAsBytes(d);
				request.add(new UpdateRequest(index, esId(d)).doc(json, XContentType.JSON));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		});
		return exec(bulkOperator, request);
	}

	public <T extends ElasticsearchId> Cancellable bulkUpdateAsync(String index, T source) {
		return bulkUpdateAsync(index, List.of(source));
	}

	public <T extends ElasticsearchId> Cancellable bulkUpdateAsync(String index, List<T> sources) {
		BulkRequest request = new BulkRequest();
		sources.forEach(d -> {
			try {
				byte[] json = mapper.writeValueAsBytes(d);
				request.add(new UpdateRequest(index, esId(d)).doc(json, XContentType.JSON));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		});
		return exec(bulkAsyncOperator, request);
	}

	public SearchResponse search(SearchSourceBuilder searchSourceBuilder, String... indices) {
		return search(searchSourceBuilder, "1m", indices);
	}

	public <T extends ElasticsearchId> List<T> search(SearchSourceBuilder searchSourceBuilder, String[] indices,
			Class<T> clazz) {
		SearchResponse response = search(searchSourceBuilder, "1m", indices);
		List<T> rs = new ArrayList<>();
		SearchHit[] hits = response.getHits().getHits();
		for (SearchHit h : hits) {
			try {
				rs.add(mapper.readValue(h.getSourceAsString(), clazz));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return rs;
	}

	public SearchResponse search(SearchSourceBuilder searchSourceBuilder, String keepAlive, String[] indices) {
		SearchRequest request = new SearchRequest(indices);
		request.source(searchSourceBuilder);
		if (keepAlive == null) {
			keepAlive = "1m";
		}
		request.scroll(keepAlive);
		return exec(searchOperator, request);
	}

	public SearchResponse searchScroll(String scrollId) {
		return searchScroll(scrollId, null);
	}

	public SearchResponse searchScroll(String scrollId, String keepAlive) {
		SearchScrollRequest request = new SearchScrollRequest(scrollId);
		if (keepAlive != null) {
			request.scroll(keepAlive);
		}
		return exec(searchScrollOperator, request);
	}

	public ClearScrollResponse clearScroll(String... scrollId) {
		return clearScroll(List.of(scrollId));
	}

	public ClearScrollResponse clearScroll(List<String> scrollIds) {
		ClearScrollRequest request = new ClearScrollRequest();
		request.scrollIds(scrollIds);
		return exec(clearScrollOperator, request);
	}

	private <T extends ElasticsearchId> String esId(T source) {
		return source.getElasticsearchId();
	}

	public <T extends ElasticsearchId> Page<T> searchScrollDeepPaging(SearchSourceBuilder searchSourceBuilder,
			int pageNumber, int pageSize, String index, Class<T> clazz) {
		return searchScrollDeepPaging(searchSourceBuilder, pageNumber, pageSize, new String[] { index }, "1m", clazz);
	}

	public <T extends ElasticsearchId> Page<T> searchScrollDeepPaging(SearchSourceBuilder searchSourceBuilder,
			int pageNumber, int pageSize, String index, String keepAlive, Class<T> clazz) {
		return searchScrollDeepPaging(searchSourceBuilder, pageNumber, pageSize, new String[] { index }, keepAlive,
				clazz);
	}

	public <T extends ElasticsearchId> Page<T> searchScrollDeepPaging(SearchSourceBuilder searchSourceBuilder,
			final int pageNumber, final int pageSize, String[] indices, String keepAlive, Class<T> clazz) {

		// fetch
		final FetchSourceContext fetch = searchSourceBuilder.fetchSource();
		final List<SortBuilder<?>> sorts = searchSourceBuilder.sorts();

		searchSourceBuilder.fetchSource(false).from(0).size(pageSize);// scroll from=0
		SearchResponse response = search(searchSourceBuilder, keepAlive, indices);

		final long total = response.getHits().getTotalHits().value;
		List<String> esIds = new LinkedList<>();
		String scrollId = null;

		loop: for (int i = 0; i < pageNumber; i++) {
			SearchHit[] hits = response.getHits().getHits();
			if (hits == null || hits.length == 0) {
				break loop;
			}
			if (i == (pageNumber - 1)) {
				for (SearchHit hit : hits) {
					esIds.add(hit.getId());
				}
				break loop;
			}
			scrollId = response.getScrollId();
			response = searchScroll(response.getScrollId(), keepAlive);
		}

		if (scrollId != null) {
			clearScroll(scrollId);
		}

		List<T> tss = (!esIds.isEmpty()) ? getIds(esIds, fetch, sorts, keepAlive, indices, clazz) : new ArrayList<>();
		return new Page<T>().setList(tss).setPageNumber(pageNumber).setPageSize(pageSize).setTotal(total);
	}

	private <T extends ElasticsearchId> List<T> getIds(List<String> esIds, FetchSourceContext fetch,
			List<SortBuilder<?>> sorts, String keepAlive, String[] indices, Class<T> clazz) {

		SearchSourceBuilder s = new SearchSourceBuilder();
		BoolQueryBuilder b = new BoolQueryBuilder();
		final int size = esIds.size();

		s.fetchSource(fetch).size(size);
		sorts.forEach(s::sort);
		s.query(b.filter(QueryBuilders.idsQuery().addIds(esIds.toArray(new String[size]))));

		SearchResponse response = search(s, keepAlive, indices);
		SearchHit[] hits = response.getHits().getHits();

		List<T> tss = new ArrayList<>();
		if (hits != null && hits.length > 0) {
			for (SearchHit hit : hits) {
				try {
					tss.add(mapper.readValue(hit.getSourceAsString(), clazz));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return tss;
	}

	public BulkByScrollResponse deleteByQuery(QueryBuilder query, String... indices) {
		DeleteByQueryRequest request = new DeleteByQueryRequest(indices);
		request.setConflicts("proceed");
		request.setQuery(query);
		request.setBatchSize(1000);
		request.setSlices(2);
		request.setScroll(TimeValue.timeValueMinutes(10));
		request.setTimeout(TimeValue.timeValueMinutes(2));
		request.setRefresh(true);
		return deleteByQuery(request);
	}

	public BulkByScrollResponse deleteByQuery(DeleteByQueryRequest request) {
		return exec(deleteByQueryRequestOperator, request);
	}

	public BulkByScrollResponse updateByQuery(QueryBuilder query, String... indices) {
		UpdateByQueryRequest request = new UpdateByQueryRequest(indices);
		request.setConflicts("proceed");
		request.setQuery(query);
		request.setBatchSize(1000);
		request.setSlices(2);
		request.setScroll(TimeValue.timeValueMinutes(10));
		request.setTimeout(TimeValue.timeValueMinutes(2));
		request.setRefresh(true);
		return updateByQuery(request);
	}

	public BulkByScrollResponse updateByQuery(UpdateByQueryRequest request) {
		return exec(updateByQueryRequestOperator, request);
	}

	public CountResponse count(QueryBuilder query, String... indices) {
		return count(new CountRequest(indices, query));
	}

	public CountResponse count(CountRequest request) {
		return exec(countRequestOperator, request);
	}

	public RestHighLevelClient client() {
		return client;
	}

	public AcknowledgedResponse deleteIndex(String... indices) {
		return deleteIndex(new DeleteIndexRequest(indices));
	}

	public AcknowledgedResponse deleteIndex(DeleteIndexRequest request) {
		return exec(deleteIndexOperator, request);
	}

	public ExplainResponse explain(ExplainRequest request) {
		return exec(explainOperator, request);
	}

	public org.elasticsearch.client.core.AcknowledgedResponse deleteAlias(DeleteAliasRequest request) {
		return exec(deleteAliasOperator, request);
	}

	public boolean exists(String index, String id) {
		return exists(new GetRequest(index, id));
	}

	public boolean exists(GetRequest request) {
		return exec(existsRequestOperator, request);
	}

	public UpdateResponse update(UpdateRequest request) {
		return exec(updateRequestOperator, request);
	}

}