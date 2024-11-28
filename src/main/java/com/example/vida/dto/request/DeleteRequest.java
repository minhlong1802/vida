package com.example.vida.dto.request;

import java.util.List;

public class DeleteRequest {
    private List<Integer> ids;

    // Getter and Setter
    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }
}