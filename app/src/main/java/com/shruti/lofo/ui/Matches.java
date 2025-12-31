package com.shruti.lofo.ui.Matches;

import com.shruti.lofo.ui.Found.FoundItems;
import com.shruti.lofo.ui.Lost.LostItems;

public class Matches {
    private LostItems lostItem;
    private FoundItems foundItem;
    private String status = "pending"; // pending, approved, rejected

    public Matches() {}

    public Matches(LostItems lostItem, FoundItems foundItem) {
        this.lostItem = lostItem;
        this.foundItem = foundItem;
        this.status = "pending";
    }

    public LostItems getLostItem() {
        return lostItem;
    }

    public FoundItems getFoundItem() {
        return foundItem;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
