package com.msme.iitism.productmanager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Products extends AppCompatActivity {

    private ListView mListView;
    private DataBaseHandler mDbHandler;
    private ProductAdapter mProductAdapter;
    private List<Product> mProductlist;
    private SparseBooleanArray mSelectedIDS=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mListView = (ListView) findViewById(R.id.allProductList);
        mDbHandler = new DataBaseHandler(this);
        mProductlist = Collections.synchronizedList(new ArrayList<Product>());
        mProductlist = mDbHandler.getAllProducts();

        mProductAdapter = new ProductAdapter(this,R.layout.product_list_item,mProductlist);

        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        //mListView.setDrawSelectorOnTop(false);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int position, long l, boolean b) {

                final int checkedCount = mListView.getCheckedItemCount();
                actionMode.setTitle(checkedCount + " Selected");
                mProductAdapter.toggleSelection(position);
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                actionMode.getMenuInflater().inflate(R.menu.cab_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId())
                {
                    case R.id.delete:
                        SparseBooleanArray selected = mProductAdapter.getSelectedIds();

                        for(int i = (selected.size()-1);i>=0;i--)
                        {
                            if(selected.valueAt(i))
                            {
                                Product selectedItem = mProductAdapter.getItem(selected.keyAt(i));
                                mProductAdapter.remove(selectedItem);
                            }
                        }
                        actionMode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                mProductAdapter.removeSelection();
                mProductAdapter = new ProductAdapter(Products.this,R.layout.product_list_item,mDbHandler.getAllProducts());
                mListView.setAdapter(mProductAdapter);
                mProductAdapter.notifyDataSetChanged();
                mProductAdapter.notifyDataSetInvalidated();
            }
        });

        mListView.setAdapter(mProductAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Product product = (Product) adapterView.getItemAtPosition(position);

                final AlertDialog alertDialog = new AlertDialog.Builder(Products.this).create();
                LayoutInflater layoutInflater = getLayoutInflater();
                View dialogView = layoutInflater.inflate(R.layout.scan_dialog_layout,null);

                final TextView pText = (TextView) dialogView.findViewById(R.id.Dproduct_id);
                final EditText qText = (EditText) dialogView.findViewById(R.id.DQuantity);

                pText.setText(product.getProduct_id());
                qText.setText(String.valueOf(product.getCount()));
                qText.setSelection(qText.getText().length());

                Button mSaveButton = (Button) dialogView.findViewById(R.id.dialogSave);
                Button mCancelButton = (Button) dialogView.findViewById(R.id.dialogCancel);

                mSaveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!qText.getText().toString().matches("")) {
                            Product product = new Product(pText.getText().toString(), Integer.parseInt(qText.getText().toString()));
                            mDbHandler.changeQuantity(product);
                            mProductAdapter.swapData(mDbHandler.getAllProducts());
                            alertDialog.dismiss();
                        }
                        else
                            showToast("Please enter valid quantity");
                    }
                });

                mCancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.setView(dialogView);
                //To Prevent dismiss on backKey Press
                alertDialog.setCancelable(false);
                //To Prevent dismiss on outside Touch
                alertDialog.setCanceledOnTouchOutside(false);

                alertDialog.show();
            }
        });
    }

    public void showToast(String text)
    {
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.products_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id)
        {
            case R.id.printIntent1:
                Intent intent = new Intent(this,Print.class);
                startActivity(intent);
        }
        return  super.onOptionsItemSelected(item);
    }
}
