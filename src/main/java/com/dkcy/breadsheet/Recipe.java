package com.dkcy.breadsheet;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by danielyeo on 27/07/2016.
 */

public class Recipe {
    //private variables
    private int _id;
    private String _name;
    private String[][] ingredients;

    // Empty constructor
    public Recipe(){

    }
    // constructor
    public Recipe(int id, String name, String[][] ingredients){
        this._id = id;
        this._name = name;
        this.ingredients = ingredients;
    }

    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }

    // getting name
    public String getName(){
        return this._name;
    }

    // setting name
    public void setName(String name){
        this._name = name;
    }

    // getting ingredients
    public String[][] getIngredients(){
        return this.ingredients;
    }

    // setting ingredients
    public void setIngredients(String[][] ingredients){
        this.ingredients = ingredients;
    }
}
