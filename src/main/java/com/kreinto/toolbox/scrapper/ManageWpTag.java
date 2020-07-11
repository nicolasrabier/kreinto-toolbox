package com.kreinto.toolbox.scrapper;

public enum ManageWpTag {

    NO_UPDATE("no-update"), NO_CACHE("no-cache"), FRANCE("france");

    private final String tag;

    private ManageWpTag(String tag){
        this.tag = tag;
    }

    public String toString(){
        return tag;
    }
}
