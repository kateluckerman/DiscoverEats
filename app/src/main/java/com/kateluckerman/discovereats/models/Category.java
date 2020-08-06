package com.kateluckerman.discovereats.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Category")
public class Category extends ParseObject {

    public static final String CLASS_NAME = "Category";
    public static final String KEY_ALIAS = "alias";
    public static final String KEY_TITLE = "title";

    public Category() {}

    public String getAlias() {
        return getString(KEY_ALIAS);
    }

    public String getTitle() {
        return getString(KEY_TITLE);
    }
}
