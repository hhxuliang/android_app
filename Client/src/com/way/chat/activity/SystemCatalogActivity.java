package com.way.chat.activity;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.way.chat.activity.R;
import com.way.chat.activity.R.id;
import com.way.chat.activity.R.layout;


import android.R.drawable;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SystemCatalogActivity extends ListActivity 
{
	private List<String> items = null;//�������  
    private List<String> paths = null;//���·��  
    private String rootPath = "/mnt";  
    private TextView tv;
    public   String  selectFile;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);  
        setContentView(R.layout.catalog);  
        tv = (TextView) this.findViewById(R.id.catalog_tv);  
        this.getFileDir(rootPath);//��ȡrootPathĿ¼�µ��ļ�. 
	} 
	public void getFileDir(String filePath) {  
        try
        {  
            this.tv.setText("��ǰ·��:"+filePath);// ���õ�ǰ����·��  
            items = new ArrayList<String>();  
            paths = new ArrayList<String>();  
            File f = new File(filePath);  
            File[] files = f.listFiles();// �г������ļ�  
            // ������Ǹ�Ŀ¼,���г����ظ�Ŀ¼����һĿ¼ѡ��  
            if (!filePath.equals(rootPath)) 
            {  
                items.add("���ظ�Ŀ¼");  
                paths.add(rootPath);  
                items.add("������һ��Ŀ¼");  
                paths.add(f.getParent());  
            }  
            // �������ļ�����list��  
            if(files != null)
            {  
                int count = files.length;// �ļ�����  
                for (int i = 0; i < count; i++) 
                {  
                    File file = files[i];  
                    items.add(file.getName());  
                    paths.add(file.getPath());  
                }  
            }  
  
            //����ȥ��һ�������
            //this ������
            //android.R.layout.simple_list_item_1 ��Android��ʾ�б�ÿһ���Լ�������
            //item����Ǹ������Լ�����������ʾ
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, items);  
            this.setListAdapter(adapter);  
        }
        catch(Exception ex)
        {  
            ex.printStackTrace();  
        }  
  
    }  
	
	@Override  
    protected void onListItemClick(ListView l, View v, int position, long id) 
	{  
        super.onListItemClick(l, v, position, id);  
        String path = paths.get(position);  
        final File file = new File(path);  
        //������ļ��оͼ����ֽ�  
        if(file.isDirectory()){  
            this.getFileDir(path);  
        }
        else{         	
        	AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
        	alertDialog.setTitle("��ʾ");
        	alertDialog.setIcon(drawable.ic_dialog_info);
        	alertDialog.setMessage(file.getName()+"ȷ��ѡ������ļ���?");
        	//��������ȷ��
        	alertDialog.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){  
	                public void onClick(DialogInterface dialog, int which)  {  
	                         //ִ��ɾ��������ʲô����������
	                		File delFile=new File(file.getAbsolutePath());
	                		if(delFile.exists()){
	                			Log.i("PATH",delFile.getAbsolutePath());
	                			/*delFile.delete();
	                			//ˢ�½���
	                			getFileDir(file.getParent());	*/
	                			
	                			//��ѡ����ļ�·�����ݸ��ϴ��ļ�·��picPath
	                			CramerProActivity.picPath=file.getPath();
	                			CramerProActivity.txt.setText("�ļ�·��:"+file.getPath());
	                			Toast.makeText(SystemCatalogActivity.this,"��ѡ���ļ�"+file.getName()+"\n"+"����ϴ�", Toast.LENGTH_LONG).show();
	                			Intent intent = new Intent(SystemCatalogActivity.this, CramerProActivity.class);  
	                            startActivity(intent);  
	                            finish(); 
	                		}
	                }  
	            }
            );
        	//�����ұ�ȡ��
        	alertDialog.setNegativeButton("ȡ��", new DialogInterface.OnClickListener(){  
	                public void onClick(DialogInterface dialog, int which) {  
	                	 	//ִ�в���
	                	getFileDir(file.getParent());
	                }  
	            }
        	);
        	alertDialog.show();
        }  
    }  
}