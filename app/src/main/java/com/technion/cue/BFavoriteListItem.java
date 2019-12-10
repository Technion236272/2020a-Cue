package com.technion.cue;

public class BFavoriteListItem {

    private String favorite_business_ref;

    public BFavoriteListItem() {}

    public BFavoriteListItem(String favorite_business_ref) {
        this.favorite_business_ref=favorite_business_ref;
    }


    public String getFavorite_business_ref() {
        return favorite_business_ref;
    }
}
