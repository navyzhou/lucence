package com.yc.lucence.dao;

import java.util.List;

import com.yc.lucence.entity.NewsInfo;

public class NewsInfoDao {
	public List<NewsInfo> findAll() {
		DBHelper db = new DBHelper();
		String sql = "select nid, title, ndate, author, content from newsInfo";
		return db.finds(sql, NewsInfo.class);
	}
}
