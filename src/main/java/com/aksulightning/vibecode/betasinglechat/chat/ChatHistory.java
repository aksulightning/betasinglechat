package com.aksulightning.vibecode.betasinglechat.chat;

import java.util.ArrayList;
import java.util.List;

public final class ChatHistory {
    private static final int MAX_ENTRIES = 100;
    private final List<String> entries = new ArrayList<>();

    public void add(String message) {
        if (message.isEmpty() || (!entries.isEmpty()
                && entries.get(entries.size() - 1).equals(message))) {
            return;
        }
        if (entries.size() == MAX_ENTRIES) {
            entries.remove(0);
        }
        entries.add(message);
    }

    public int size() {
        return entries.size();
    }

    public String get(int index) {
        return entries.get(index);
    }
}
