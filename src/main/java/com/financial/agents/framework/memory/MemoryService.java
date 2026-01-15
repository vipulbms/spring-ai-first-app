package com.financial.agents.framework.memory;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing conversation memory for agents
 * Uses LangChain4j's ChatMemory for context retention
 */
@Slf4j
@Service
public class MemoryService {
    
    // Store memory per request/session
    private final Map<String, ChatMemory> memoryStore = new ConcurrentHashMap<>();
    private final int maxMessages = 50; // Keep last 50 messages per session
    
    /**
     * Get or create chat memory for a request
     */
    public ChatMemory getOrCreateMemory(String requestId) {
        return memoryStore.computeIfAbsent(requestId, id -> {
            log.debug("Creating new chat memory for request: {}", id);
            return MessageWindowChatMemory.withMaxMessages(maxMessages);
        });
    }
    
    /**
     * Get existing memory for a request
     */
    public ChatMemory getMemory(String requestId) {
        return memoryStore.get(requestId);
    }
    
    /**
     * Clear memory for a request
     */
    public void clearMemory(String requestId) {
        ChatMemory memory = memoryStore.remove(requestId);
        if (memory != null) {
            log.debug("Cleared memory for request: {}", requestId);
        }
    }
    
    /**
     * Clear all memories (use with caution)
     */
    public void clearAllMemories() {
        memoryStore.clear();
        log.info("Cleared all chat memories");
    }
    
    /**
     * Check if memory exists for a request
     */
    public boolean hasMemory(String requestId) {
        return memoryStore.containsKey(requestId);
    }
}
