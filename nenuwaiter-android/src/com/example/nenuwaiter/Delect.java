package com.example.nenuwaiter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class Delect extends SimpleAdapter {
	private Context context;
	ArrayList<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
	@SuppressWarnings("unchecked")
	public Delect(Context context, List<? extends Map<String, ?>> data,
			int resource, String[] from, int[] to) {

		super(context, data, resource, from, to);
        this.context = context;
        list = (ArrayList<Map<String, Object>>) data;
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View v = super.getView(position, convertView, parent);
//        View row = null;
//        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
//        row =inflater.inflate(R.layout.shop_list, null);
        Button del = (Button) v.findViewById(R.id.delect);
        del.setTag(position);
        del.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				

                Map<String,Object> item =  list.get((Integer) v.getTag());
                String one = item.get("id_vlu").toString();
                Integer index = (Integer) v.getTag();
                list.remove(index.intValue()); 
				notifyDataSetChanged();
				String DB_PATH = "/data/data/com.example.nenuwaiter/databases/";
				String DB_NAME = "order.db";
				SQLiteDatabase sampleDB =  SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.CREATE_IF_NECESSARY);
				sampleDB.delete("orderlist", "id = ?", new String[]{one});
				Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
				sampleDB.close();
				
				
			}
		});
		return v;
		
	}
	

}
