package com.shruti.lofo.ui.DashBoard;

import androidx.lifecycle.ViewModel;

public class DashBoardViewModel extends ViewModel {
    private String itemId; // Firestore document ID
    private String imageURI;
    private String category;
    private String description;
    private String ownerName;
    private String finderName;
    private String tag;
    private String dateLost;
    private String dateFound;
    private String itemName;
    private String collectionName;

    // Default constructor (required for Firestore)
    public DashBoardViewModel() {}

    // Full constructor for convenience
    public DashBoardViewModel(String imageURI, String category, String description,
                              String ownerName, String finderName, String tag,
                              String dateLost, String itemName, String dateFound) {
        this.imageURI = imageURI;
        this.category = category;
        this.description = description;
        this.ownerName = ownerName;
        this.finderName = finderName;
        this.itemName = itemName;
        this.tag = tag;
        this.dateLost = dateLost;
        this.dateFound = dateFound;
    }

    // 🔹 Getters and setters
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getImageURI() { return imageURI; }
    public void setImageURI(String imageURI) { this.imageURI = imageURI; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getFinderName() { return finderName; }
    public void setFinderName(String finderName) { this.finderName = finderName; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getDateLost() { return dateLost; }
    public void setDateLost(String dateLost) { this.dateLost = dateLost; }

    public String getDateFound() { return dateFound; }
    public void setDateFound(String dateFound) { this.dateFound = dateFound; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCollectionName() { return collectionName; }
    public void setCollectionName(String collectionName) { this.collectionName = collectionName; }
}
