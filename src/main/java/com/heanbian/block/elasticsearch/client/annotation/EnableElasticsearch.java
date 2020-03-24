package com.heanbian.block.elasticsearch.client.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.heanbian.block.elasticsearch.client.autoconfigure.ElasticsearchConfiguration;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(ElasticsearchConfiguration.class)
@Documented
public @interface EnableElasticsearch {
}
