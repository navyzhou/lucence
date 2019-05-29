package com.yc.lucence.util;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @company 源辰信息
 */
public class LucenceUtil {
	public static String indexPath = "index";
	private String path;
	private IKAnalyzer ikAnalyzer;
	private String idField;
	private String[] textFields;

	/**
	 * textFields的字段类型为分词索引且存储，
	 * 如果不设置textFields，请不要使用带反射的方法，
	 * 如果不设置idField，则追加不能使用以id进行删除的delete方法。
	 * 
	 * @param className		类名（用于区分索引字典存储路径）
	 * @param idField		id字段
	 * @param textFields	索引字段，即从哪些字段中查找
	 */
	public LucenceUtil(String className, String idField, String[] textFields) {
		ikAnalyzer = new IKAnalyzer();
		path = indexPath + "/" + className;
		this.idField = idField;
		this.textFields = textFields;
	}

	/**
	 * 添加多个对象到索引字典
	 * @param c 存在集合中的实体类对象
	 * @param objects 从哪些数据中分词搜索
	 */
	public <T> void add(Class<T> c, List<T> objects) {
		IndexWriter writer = null;
		try {
			Directory index = FSDirectory.open(Paths.get(path));
			writer = new IndexWriter(index, new IndexWriterConfig(ikAnalyzer));
			for (T object : objects) {
				Document doc = new Document();
				doc.add(new StringField(idField, this.getValue(c, object, idField), Field.Store.YES));
				for (String textField : textFields) {
					doc.add(new TextField(textField, this.getValue(c, object, textField), Field.Store.YES)); // 只用于查询而不用于显示可以为NO（请修改add和update）
				}
				writer.addDocument(doc);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}  finally {
			this.closeAll(writer, null);
		}
	}

	/**
	 * 添加单个对象到索引字典
	 */
	public <T> void add(Class<T> c, T object) {
		IndexWriter writer = null;
		try {
			Directory index = FSDirectory.open(Paths.get(path));
			writer = new IndexWriter(index, new IndexWriterConfig(ikAnalyzer));
			Document doc = new Document();
			doc.add(new StringField(idField, this.getValue(c, object, idField), Field.Store.YES));
			for (String textField : textFields) {
				doc.add(new TextField(textField, this.getValue(c, object, textField), Field.Store.YES));
			}
			writer.addDocument(doc);
		} catch (IOException e) {
			e.printStackTrace();
		}  finally {
			this.closeAll(writer, null);
		}
	}

	/**
	 * 以Document列表的形式添加到索引字典，可以自定义存储的字段名及其字段类型（是否索引，是否分词，是否存储）
	 */
	public void add(List<Document> docs) {
		IndexWriter writer = null;
		try {
			Directory index = FSDirectory.open(Paths.get(path));
			writer = new IndexWriter(index, new IndexWriterConfig(ikAnalyzer));
			for (Document doc : docs) {
				writer.addDocument(doc);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}  finally {
			this.closeAll(writer, null);
		}
	}

	/**
	 * 用新对象更新索引字典
	 */
	public <T> void update(Class<T> c, T object) {
		IndexWriter writer = null;
		try {
			Directory index = FSDirectory.open(Paths.get(path));
			writer = new IndexWriter(index, new IndexWriterConfig(ikAnalyzer));
			Document doc = new Document();
			doc.add(new StringField(idField, this.getValue(c, object, idField), Field.Store.YES));
			for (String textField : textFields) {
				doc.add(new TextField(textField, this.getValue(c, object, textField), Field.Store.YES));
			}
			writer.updateDocument(new Term(idField, this.getValue(c, object, idField)), doc);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			this.closeAll(writer, null);
		}
	}

	/**
	 * 自定义的方式更新索引字典
	 */
	public void update(Document doc, Term term) {
		IndexWriter writer = null;
		try {
			Directory index = FSDirectory.open(Paths.get(path));
			writer = new IndexWriter(index, new IndexWriterConfig(ikAnalyzer));
			writer.updateDocument(term, doc);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			this.closeAll(writer, null);
		}
	}

	/**
	 * 自定义的方式从索引字典删除记录
	 */
	public void delete(Term term) {
		IndexWriter writer = null;
		try {
			Directory index = FSDirectory.open(Paths.get(path));
			writer = new IndexWriter(index, new IndexWriterConfig(ikAnalyzer));
			writer.deleteDocuments(term);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			this.closeAll(writer, null);
		}
	}

	/**
	 * 根据id从索引字典删除记录
	 */
	public void delete(String id) {
		IndexWriter writer = null;
		try {
			Directory index = FSDirectory.open(Paths.get(path));
			writer = new IndexWriter(index, new IndexWriterConfig(ikAnalyzer));
			writer.deleteDocuments(new Term(idField, id));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			this.closeAll(writer, null);
		}
	}

	/**
	 * 清空索引字典的记录
	 */
	public void deleteAll() {
		IndexWriter writer = null;
		try {
			Directory index = FSDirectory.open(Paths.get(path));
			writer = new IndexWriter(index, new IndexWriterConfig(ikAnalyzer));
			writer.deleteAll();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			this.closeAll(writer, null);
		}
	}

	/**
	 * @param fields	查询字段
	 * @param keyword	查询关键字
	 * @param limit	返回的最大记录数
	 * @return			查询到的Document列表，根据相关度排序
	 */
	public List<Document> search(String[] fields, String keyword, int limit) {
		IndexReader reader = null;
		List<Document> result = new ArrayList<>();
		try {
			Directory index = FSDirectory.open(Paths.get(path));
			reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			Query query = new MultiFieldQueryParser(fields, ikAnalyzer).parse(keyword);
			ScoreDoc[] scoreDocs = searcher.search(query, limit).scoreDocs;
			for (ScoreDoc scoreDoc : scoreDocs) {
				int docId = scoreDoc.doc;
				Document doc = searcher.doc(docId);
				result.add(doc);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
			this.closeAll(null, reader);
		}
		return result;
	}

	/**
	 * 在原始搜索方法上添加了字段的权重，用于影响返回数据的相关度排序
	 * 
	 * @param weights	fields中各字段的权重值
	 */
	public List<Document> search(String[] fields, String keyword, Float[] weights, int limit) {
		IndexReader reader = null;
		List<Document> result = new ArrayList<>();
		try {
			Directory index = FSDirectory.open(Paths.get(path));
			reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			Map<String, Float> boosts = new HashMap<>();
			for (int i = 0, len = Math.min(fields.length, weights.length); i < len; i++) {
				boosts.put(fields[i], weights[i]);
			}
			Query query = new MultiFieldQueryParser(fields, ikAnalyzer, boosts).parse(keyword);
			ScoreDoc[] scoreDocs = searcher.search(query, limit).scoreDocs;
			for (ScoreDoc scoreDoc : scoreDocs) {
				int docId = scoreDoc.doc;
				Document doc = searcher.doc(docId);
				result.add(doc);
				//                System.out.println("权重：" + scoreDoc.score + "\t" + "pname：" + doc.get("pname"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
			this.closeAll(null, reader);
		}
		return result;
	}

	private <T> String getValue(Class<T> c, T object, String field) {
		String value = null;
		try{
			String getterName = "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
			Method getter = c.getDeclaredMethod(getterName);
			value = String.valueOf(getter.invoke(object));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return value;
	}

	private void closeAll(IndexWriter writer, IndexReader reader) {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
