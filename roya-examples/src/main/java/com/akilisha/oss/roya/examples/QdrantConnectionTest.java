package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;

import java.util.Collections;

/**
 * Simple Qdrant Connectivity Test
 * 
 * This is a minimal test to verify Qdrant connection works.
 * It tests the exact connectivity check that happens in LangChainAdapter.getQdrantConnection()
 * 
 * Run:
 * ./gradlew :roya-examples:run --args="QdrantConnectionTest"
 */
public class QdrantConnectionTest {

    public static void main(String[] args) {
        System.out.println("🔍 Qdrant Connectivity Test\n");
        System.out.println("═══════════════════════════════════════════════════════════\n");

        // Test 1: REST API check (port 6333)
        System.out.println("Test 1: Checking REST API (port 6333)...");
        try {
            java.net.URL restUrl = new java.net.URL("http://localhost:6333/");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) restUrl.openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            
            System.out.println("   ✅ REST API responded with code: " + responseCode);
            System.out.println("   ✅ Qdrant REST API is accessible at http://localhost:6333\n");
        } catch (Exception e) {
            System.out.println("   ❌ REST API check failed: " + e.getMessage());
            System.out.println("   Error type: " + e.getClass().getSimpleName() + "\n");
        }

        // Test 2: Initialize AI plugin and test via LangChainAdapter
        System.out.println("Test 2: Testing via LangChainAdapter (gRPC port 6334)...");
        try {
            var app = Roya.create();
            var aiPlugin = new AIPlugin();
            aiPlugin.register(app.services());
            aiPlugin.start();
            
            AI ai = app.services().get(AI.class);
            
            // This will trigger getQdrantConnection() internally
            System.out.println("   Attempting to index empty collection to test connection...");
            ai.vectors().index("test-collection", Collections.emptyList());
            System.out.println("   ✅ gRPC connection successful!");
            System.out.println("   ✅ LangChainAdapter.getQdrantConnection() works correctly\n");
            
        } catch (Exception e) {
            System.out.println("   ❌ LangChainAdapter connection failed: " + e.getMessage());
            System.out.println("   Error type: " + e.getClass().getSimpleName());
            
            // Print stack trace to see where it failed
            System.out.println("\n   Stack trace:");
            e.printStackTrace();
            System.out.println();
        }

        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("Test complete!");
    }
}

