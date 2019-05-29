package com.yc.lucence.test;

import java.util.List;

import org.apache.lucene.document.Document;
import org.junit.Test;

import com.yc.lucence.dao.NewsInfoDao;
import com.yc.lucence.entity.NewsInfo;
import com.yc.lucence.util.LucenceUtil;

public class LucenceTest {
	// 初始化lucence工具类
	LucenceUtil lu = new LucenceUtil("NewsInfo", "nid", new String[] {"title", "content"});
	
	/**
	 * 演示初始化字典和搜索功能
	 */
	@Test
	public void test1() {
		// 清空字典
		lu.deleteAll();
		
		// 初始化数据源
		NewsInfoDao newsInfoDao = new NewsInfoDao();
		List<NewsInfo> list = newsInfoDao.findAll();
		
		// 第一步：添加
		lu.add(NewsInfo.class, list);
		
		// 第二步：搜索
		List<Document> docs = lu.search(new String[] {"title", "content"}, "中国", 100);
		
		// 第三步：取值
		for(Document doc : docs) {
			System.out.println(doc.get("nid") + "\t" + doc.get("title") + "\t" + doc.get("content"));
		}
	}
}



/*		// 可选：删除和更新
lu.delete("6");
lu.update(Product.class, new Product(3, "月光灯", "亮度大，照射范围广。"));*/













