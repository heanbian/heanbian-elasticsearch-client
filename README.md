# heanbian-elasticsearch

## 依赖条件

JDK 11

Elasticsearch 7.13.3

## pom.xml

具体版本，可以从 Maven Central 获得

```
<dependency>
  <groupId>com.heanbian.block</groupId>
  <artifactId>heanbian-elasticsearch</artifactId>
  <version>1.1.5</version>
</dependency>
```

## 使用示例

```
String connectionString = "elasticsearch://<username>:<password>@<ip1>:<port1>,<ip2>:<port2>";
ElasticsearchTemplate esTemplate = new ElasticsearchTemplate(connectionString);
```

说明：暂无。