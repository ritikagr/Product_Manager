package com.msme.iitism.productmanager;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ritik on 10-10-2016.
 */
public class DataBaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "product_db";
    private static final String TABLE_NAME = "products";

    private static final String KEY_PRODUCT_ID = "product_id";
    private static final String KEY_COUNT = "count";
    public DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String create_sql = "create table "+ TABLE_NAME + "("+KEY_PRODUCT_ID +" TEXT primary key not null, "+KEY_COUNT + " int not null)";
        db.execSQL(create_sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists "+ TABLE_NAME);
        onCreate(db);
    }

    public boolean isExists(String product_id)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        //String sql = "select * from "+TABLE_NAME + " where "+KEY_PRODUCT_ID + "=" + product_id;
        //Cursor cursor = db.rawQuery(sql,null);
        Cursor cursor = db.query(TABLE_NAME,new String[]{KEY_PRODUCT_ID,KEY_COUNT},KEY_PRODUCT_ID + " = ?",new String[]{product_id},null,null,null,null);

        if(cursor.moveToFirst())
            return true;
        else
            return false;
    }

    public void add_product(Product product)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //Log.i("Add",product.getProduct_id().toString());
        values.put(KEY_PRODUCT_ID,product.getProduct_id().toString());
        values.put(KEY_COUNT,String.valueOf(product.getCount()));

        db.insert(TABLE_NAME,null,values);
        db.close();
    }

    public Product get_product(String id)
    {
        Product product = new Product();
        return product;
    }

    public List<Product> getAllProducts()
    {
        List<Product> productList = Collections.synchronizedList(new ArrayList<Product>());

        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "select * from " + TABLE_NAME;
        Cursor cursor = db.rawQuery(sql,null);

        if(cursor.moveToFirst())
        {
            do{
                Product product = new Product();
                product.setProduct_id(cursor.getString(0));
                product.setCount(Integer.parseInt(cursor.getString(1)));

                productList.add(product);
            }while(cursor.moveToNext());
        }
        db.close();
        return productList;
    }

    public int deleteProduct(Product product)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME,new String[]{KEY_PRODUCT_ID,KEY_COUNT},KEY_PRODUCT_ID + " = ?",new String[]{product.getProduct_id().toString()},null,null,null,null);

        db = this.getWritableDatabase();
        if(cursor.moveToFirst()) {
            if (Integer.parseInt(cursor.getString(1)) - product.getCount() > 0) {
                ContentValues values = new ContentValues();
                values.put(KEY_COUNT, Integer.parseInt(cursor.getString(1)) - product.getCount());

                return db.update(TABLE_NAME, values, KEY_PRODUCT_ID + "= ?", new String[]{product.getProduct_id().toString()});
            } else if (Integer.parseInt(cursor.getString(1)) - product.getCount() == 0) {
                db.delete(TABLE_NAME, KEY_PRODUCT_ID + "=?", new String[]{product.getProduct_id().toString()});
                db.close();
                return 1;
            } else {
                db.delete(TABLE_NAME, KEY_PRODUCT_ID + "=?", new String[]{product.getProduct_id().toString()});
                db.close();
                return 10 + Integer.parseInt(cursor.getString(1));
            }
        }
        return -1;
    }

    public int updateProduct(Product product)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME,new String[]{KEY_PRODUCT_ID,KEY_COUNT},KEY_PRODUCT_ID + " = ?",new String[]{product.getProduct_id().toString()},null,null,null,null);

        cursor.moveToFirst();
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_COUNT, String.valueOf(product.getCount() + Integer.parseInt(cursor.getString(1))));
        return db.update(TABLE_NAME,values,KEY_PRODUCT_ID + "= ?",new String[]{product.getProduct_id().toString()});
    }

    public int changeQuantity(Product product)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME,new String[]{KEY_PRODUCT_ID,KEY_COUNT},KEY_PRODUCT_ID + " = ?",new String[]{product.getProduct_id().toString()},null,null,null,null);

        cursor.moveToFirst();
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_COUNT, String.valueOf(product.getCount()));
        return db.update(TABLE_NAME,values,KEY_PRODUCT_ID + "= ?",new String[]{product.getProduct_id().toString()});
    }

}
