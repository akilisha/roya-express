package com.akilisha.oss.roya.workflow.streaming;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

/**
 * A workflow node that can stream results incrementally.
 * Useful for AI model responses that stream tokens.
 */
public interface StreamingNode extends WorkflowNode {
    
    /**
     * Stream results as they become available
     */
    Flow.Publisher<StreamChunk> stream(NodeInput input);
    
    /**
     * Default implementation: collect all chunks into a single output
     */
    @Override
    default CompletableFuture<NodeOutput> execute(NodeInput input) {
        return collectStream(stream(input));
    }
    
    /**
     * Collect a stream into a single NodeOutput
     */
    default CompletableFuture<NodeOutput> collectStream(Flow.Publisher<StreamChunk> publisher) {
        CompletableFuture<NodeOutput> future = new CompletableFuture<>();
        StringBuilder accumulated = new StringBuilder();
        
        publisher.subscribe(new Flow.Subscriber<>() {
            private Flow.Subscription subscription;
            
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(Long.MAX_VALUE);
            }
            
            @Override
            public void onNext(StreamChunk chunk) {
                accumulated.append(chunk.content());
            }
            
            @Override
            public void onError(Throwable throwable) {
                future.complete(NodeOutput.failure(throwable));
            }
            
            @Override
            public void onComplete() {
                future.complete(NodeOutput.success("content", accumulated.toString()));
            }
        });
        
        return future;
    }
}
