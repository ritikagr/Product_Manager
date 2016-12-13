package com.msme.iitism.productmanager;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Ritik on 21-10-2016.
 */
public class ProductAdapter extends ArrayAdapter<Product> {
    Context context;
    int resource_id;
    LayoutInflater layoutInflater;
    List<Product> data = null;
    private SparseBooleanArray mSelectedItemsIds;
    private DataBaseHandler mDbHandler;

    public ProductAdapter(Context context1, int rid,List<Product> pList)
    {
        super(context1,rid,pList);
        this.context = context1;
        this.resource_id = rid;
        this.data = pList;
        mSelectedItemsIds = new SparseBooleanArray();
        mDbHandler = new DataBaseHandler(getContext());
    }

    public String getPid(int position)
    {
        Product prod = data.get(position);
        return prod.getProduct_id();
    }
    @Override
    public View getView(final int Position, final View convert_view, ViewGroup parent)
    {
        View row = convert_view;
        ProductHolder holder = null;
        if(row == null)
        {
            //LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            //row = inflater.inflate(resource_id, parent, false);

            row = (LinearLayout) LayoutInflater.from(context).inflate(resource_id,null);

            holder = new ProductHolder();
            holder.product_id = (TextView)row.findViewById(R.id.list_item_id);
            holder.product_quantity = (TextView)row.findViewById(R.id.list_item_quantity);
            row.setTag(holder);
        }
        else
        {
            holder = (ProductHolder)row.getTag();
        }

        mSelectedItemsIds = getSelectedIds();
        if(mSelectedItemsIds.get(Position))
            row.setBackgroundColor(Color.parseColor("#64B5F6"));
        else
            row.setBackgroundColor(Color.WHITE);
        try {
            final Product prod = data.get(Position);
            holder.product_id.setText(prod.getProduct_id());
            holder.product_quantity.setText(String.valueOf(prod.getCount()));
        }catch (Exception e){
            e.printStackTrace();
        }
        return row;
    }

    @Override
    public void remove(Product object) {
        data.remove(object);
        mDbHandler.deleteProduct(object);
        notifyDataSetChanged();
    }

    public List<Product> getProducts()
    {
        return data;
    }

    public void toggleSelection(int position)
    {
        selectView(position,!mSelectedItemsIds.get(position));
    }

    public void removeSelection()
    {
        mSelectedItemsIds = new SparseBooleanArray();
    }

    public void selectView(int position,boolean value)
    {
        if(value) {
            mSelectedItemsIds.put(position, value);
        }
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public int getSelectedCount(){
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds()
    {
        return mSelectedItemsIds;
    }

    public void swapData(List<Product> data)
    {
        this.data = data;
        this.notifyDataSetChanged();
    }

    public class ProductHolder{
        TextView product_id;
        TextView product_quantity;
    }

}