package org.example.Domain.Models.Badge.Request;

import lombok.Getter;
import lombok.Setter;

public class CreateBadgeRequest {
    @Getter @Setter
    private String name;

    public CreateBadgeRequest(String name) {
        this.name = name;
    }

    public CreateBadgeRequest() {
    }
}
