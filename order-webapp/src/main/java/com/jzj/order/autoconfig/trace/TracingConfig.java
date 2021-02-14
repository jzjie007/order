package com.jzj.order.autoconfig.trace;

import brave.CurrentSpanCustomizer;
import brave.SpanCustomizer;
import brave.Tracing;
import brave.context.slf4j.MDCScopeDecorator;
import brave.http.HttpTracing;
import brave.propagation.B3Propagation;
import brave.propagation.ExtraFieldPropagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.rpc.RpcTracing;
import brave.sampler.Sampler;
import brave.servlet.TracingFilter;
import brave.spring.webmvc.SpanCustomizingAsyncHandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import zipkin2.Span;
import zipkin2.codec.Encoding;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

import javax.servlet.Filter;

/**
 * @author jzjie
 */
@Configuration
@Import(SpanCustomizingAsyncHandlerInterceptor.class)
public class TracingConfig extends WebMvcConfigurerAdapter {


    @Bean
    @ConditionalOnProperty(
            value = {"zipkin.enable"},
            matchIfMissing = false)
    Sender sender(@Value("${zipkin.base.url}") String url) {
        return OkHttpSender.newBuilder()
                .encoding(Encoding.PROTO3)
                .endpoint(url)
                .build();
    }


    /**
     * Configuration for how to buffer spans into messages for Zipkin
     */
    @Bean
    @ConditionalOnBean(Sender.class)
    AsyncReporter<Span> spanReporter(Sender sender) {
        AsyncReporter.Builder builder = AsyncReporter.builder(sender);
        builder.queuedMaxSpans(50000);
        builder.queuedMaxBytes(104857600);
        return builder.build();
    }


    /**
     * Controls aspects of tracing such as the service name that shows up in the UI
     */
    @Bean
    Tracing tracing(@Value("${dubbo.application.name}") String applicationName, @Value("${zipkin.enable:false}") Boolean enable, @Autowired(required = false) AsyncReporter spanReporter) {
        Tracing.Builder builder = Tracing.newBuilder()
                .localServiceName(applicationName)
                .propagationFactory(ExtraFieldPropagation.newFactory(B3Propagation.FACTORY, "user-name"))
                .currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder()
                        // puts trace IDs into logs
                        .addScopeDecorator(MDCScopeDecorator.create())
                        .build()
                );
        if (enable) {
            builder.spanReporter(spanReporter);
            builder.sampler(Sampler.ALWAYS_SAMPLE);
        } else {
            builder.sampler(Sampler.NEVER_SAMPLE);
        }
        return builder.build();
    }

    @Bean
    SpanCustomizer spanCustomizer(Tracing tracing) {
        return CurrentSpanCustomizer.create(tracing);
    }

    /**
     * Decides how to name and tag spans. By default they are named the same as the http method
     */
    @Bean
    HttpTracing httpTracing(Tracing tracing) {
        return HttpTracing.create(tracing);
    }


    @Bean
    RpcTracing rpcTracing(Tracing tracing) {
        return RpcTracing.create(tracing);
    }

    /**
     * Creates server spans for http requests
     */
    @Bean
    Filter tracingFilter(HttpTracing httpTracing) {
        return TracingFilter.create(httpTracing);

    }

    @Autowired
    SpanCustomizingAsyncHandlerInterceptor webMvcTracingCustomizer;

    /**
     * Decorates server spans with application-defined web tags
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(webMvcTracingCustomizer);
    }

}