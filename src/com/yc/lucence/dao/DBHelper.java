package com.yc.lucence.dao;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yc.lucence.util.LogUtil;

public class DBHelper {
	
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		}
	}

	/**
	 * 获取数据库连接的方法
	 * @return
	 */
	private Connection getConnection(){
		Connection con=null;
		try {
//			Context context=new InitialContext();
//			DataSource dataSource=(DataSource) context.lookup("java:comp/env/yc");
//			con=dataSource.getConnection();
			con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/news", "root", "a");
		} catch (Exception e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		}
		return con;
	}

	/**
	 * 关闭资源
	 * @param rs 要关闭的结果集
	 * @param pstmt 要关闭的预编译块
	 * @param con 要关闭的连接
	 */
	private void closeAll(ResultSet rs,PreparedStatement pstmt,Connection con){
		if(rs!=null){
			try {
				rs.close();
			} catch (SQLException e) {
				LogUtil.log.error(e);
			}
		}

		if(pstmt!=null){
			try {
				pstmt.close();
			} catch (SQLException e) {
				LogUtil.log.error(e);
			}
		}

		if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				LogUtil.log.error(e);
			}
		}
	}

	/**
	 * 给预编译语句中的占位符赋值
	 * @param pstmt
	 * @param params
	 */
	private void setValue(PreparedStatement pstmt,Object ... params){
		if(params!=null){
			for(int i=0,len=params.length;i<len;i++){
				try {
					pstmt.setObject(i+1,params[i]);
				} catch (SQLException e) {
					LogUtil.log.error("第 "+ (i+1) +" 个占位符注值失败....");
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 给预编译语句中的占位符赋值
	 * @param pstmt
	 * @param params
	 */
	private void setValue(PreparedStatement pstmt,List<Object> params){
		if(params!=null){
			for(int i=0,len=params.size();i<len;i++){
				try {
					pstmt.setObject(i+1,params.get(i));
				} catch (SQLException e) {
					LogUtil.log.error("第 "+ (i+1) +" 个占位符注值失败....");
					e.printStackTrace();
				}
			}
		}
	}

	private void setValues(PreparedStatement pstmt,List<String> params){
		if(params!=null){
			for(int i=0,len=params.size();i<len;i++){
				try {
					pstmt.setString(i+1,params.get(i));
				} catch (SQLException e) {
					LogUtil.log.error("第 "+ (i+1) +" 个占位符注值失败....");
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 获取指定结果集中列的列名
	 * @param rs
	 * @return
	 */
	private String[] getColumnNames(ResultSet rs){
		String[] cols=null;
		if(rs!=null){
			try {
				ResultSetMetaData rsmd=rs.getMetaData();
				int count=rsmd.getColumnCount();
				cols=new String[count];
				for(int i=0;i<count;i++){
					cols[i]=rsmd.getColumnName(i+1).toLowerCase();
				}
			} catch (SQLException e) {
				LogUtil.log.error(e);
				e.printStackTrace();
			}
		}
		return cols;
	}

	/**
	 * 更新操作
	 * @param sql 要执行的更新语句
	 * @param params 要执行更新语句中占位符 ? 的值，注意顺序必须跟sql语句中?对应的值的顺序一致
	 * @return 返回语句执行后所影响的数据的行数
	 */
	public int update(String sql,Object ... params){
		Connection con=null;
		PreparedStatement pstmt=null;
		int result=0;
		try {
			con=this.getConnection();
			pstmt=con.prepareStatement(sql);

			this.setValue(pstmt, params);

			result=pstmt.executeUpdate();
		}  catch (SQLException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} finally {
			this.closeAll(null, pstmt, con);
		}
		return result;
	}

	/**
	 * 查询
	 * @param sql 要执行的查询语句
	 * @param params 查询语句中占位符?对应的值
	 * @return 每一行记录存放到一个map中，将多行记录存放到list中
	 */
	public List<Map<String,String>> finds(String sql,Object ... params){
		List<Map<String,String>> list=new ArrayList<Map<String,String>>();
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;


		try {
			con=this.getConnection();
			pstmt=con.prepareStatement(sql);

			//给占位符赋值
			this.setValue(pstmt, params);

			//获取结果集
			rs=pstmt.executeQuery();

			//获取结果中列的列名
			String[] colNames=this.getColumnNames(rs);

			Map<String,String> map=null;
			while(rs.next()){ //每循环一次就是一行数据，所以需要将这行数据存到map中，然后将这个map存到list中
				map=new HashMap<String,String>();
				//循环所有列的列名，根据列名取出对应列的值
				for(String colName:colNames){
					map.put(colName,rs.getString(colName));
				}
				list.add(map);
			}

		} catch (SQLException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} finally {
			this.closeAll(rs, pstmt, con);
		}
		return list;
	}

	/**
	 * 查询单行的方法
	 * @param sql
	 * @param params
	 * @return
	 */
	public Map<String,String> get(String sql,List<Object> params){
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		Map<String,String> map=null;

		try {
			con=this.getConnection();
			pstmt=con.prepareStatement(sql);

			//给占位符赋值
			this.setValue(pstmt, params);

			//获取结果集
			rs=pstmt.executeQuery();

			//获取结果中列的列名
			String[] colNames=this.getColumnNames(rs);

			if(rs.next()){ //每循环一次就是一行数据，所以需要将这行数据存到map中，然后将这个map存到list中
				map=new HashMap<String,String>();
				//循环所有列的列名，根据列名取出对应列的值
				for(String colName:colNames){
					map.put(colName,rs.getString(colName));
				}
			}

		} catch (SQLException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} finally {
			this.closeAll(rs, pstmt, con);
		}
		return map;
	}

	/**
	 * 基于对象的查询
	 * @param sql 要执行的sql语句
	 * @param c 这个语句对应的实体类
	 * @param params 要执行的sql语句中占位符?的值
	 * @return
	 */
	public <T> List<T> gets(String sql,Class<T> c,List<Object> params){
		List<T> list=new ArrayList<T>();
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;

		try {
			con=this.getConnection();
			pstmt=con.prepareStatement(sql);

			//给占位符赋值
			this.setValue(pstmt, params);

			//获取结果集
			rs=pstmt.executeQuery();



			//必须提前约定，属性名必须跟结果集中的列名相同。如果不一致，则我们就不知道这个列的值应该注入到对象的哪个属性中。如果列名跟属性名不同时，我们可以使用列的重命名方式，将列名重命名为对应的属性名

			//操作步骤，第一步必须获取这个结果集中所有的列的列名
			String[] colNames=this.getColumnNames(rs);

			//第二步，获取给定的这个类的所有属性名，但是类的属性是私有的，那么在这里是不能直接访问的，所以我们必须通过这个属性的setter方法来注值。所以我们直接获取这个类中的所有setter方法
			Method[] methods=c.getDeclaredMethods(); //获取这个类的所有方法
			//从这些方法中找出setter方法
			List<Method> setters=new ArrayList<Method>();
			for(Method method:methods){
				if(method.getName().startsWith("set")){
					setters.add(method);
				}
			}

			T t=null;
			String mname=null;
			Class<?>[] types;
			String typeName=null;
			while(rs.next()){ //每循环一次就是一行数据，每一行数据就是一个对象
				t=c.newInstance();//先实例化一个给定的类类型的对象 StuInfo t=new StuInfo();

				//根据结果集中每个类的类名找到对应的setter，然后将值注入    
				for(String colName:colNames){ //sid sname
					mname="set"+colName;
					for(Method md:setters){ //setSid  setSname
						if(mname.equalsIgnoreCase(md.getName())){//sid-> setsid  sname->setsname
							//让这个方法运行一次，这个方法运行有两个要来，第一个这个方法是哪个对象的方法,第二个是这个方法的参数（此时这个方法的参数就是这个列的值）

							//问题：setter方法中的参数的类型不一定都是String类型的->这个时候我们可以使用强制类型转换，但前提条件是我必须这个你这个方法需要的参数类型
							types=md.getParameterTypes();
							//这个方法的形参类型是一个数组，而我们的标准setter方法只需要形参，所以我们只去这个数组中的第一个
							if(types!=null && types.length>0){
								typeName=types[0].getSimpleName(); //取出第一个形参的参数名

								if("int".equals(typeName) || "Integer".equals(typeName)){
									md.invoke(t,rs.getInt(colName)); //t.setSid(1001);
								}else if("float".equals(typeName) || "Float".equals(typeName)){
									md.invoke(t,rs.getFloat(colName));
								}else if("double".equals(typeName) || "Double".equals(typeName)){
									md.invoke(t,rs.getDouble(colName));
								}else{
									md.invoke(t,rs.getString(colName));
								}
							}

						}
					}
				}
				list.add(t);
			}

		} catch (SQLException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (InstantiationException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} finally {
			this.closeAll(rs, pstmt, con);
		}
		return list;
	}

	/**
	 * 基于对象的查询
	 * @param sql 要执行的sql语句
	 * @param c 这个语句对应的实体类
	 * @param params 要执行的sql语句中占位符?的值
	 * @return
	 */
	public <T> T get(String sql,Class<T> c,List<Object> params){
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		T t=null;
		try {
			con=this.getConnection();
			pstmt=con.prepareStatement(sql);

			//给占位符赋值
			this.setValue(pstmt, params);

			//获取结果集
			rs=pstmt.executeQuery();

			//必须提前约定，属性名必须跟结果集中的列名相同。如果不一致，则我们就不知道这个列的值应该注入到对象的哪个属性中。如果列名跟属性名不同时，我们可以使用列的重命名方式，将列名重命名为对应的属性名

			//操作步骤，第一步必须获取这个结果集中所有的列的列名
			String[] colNames=this.getColumnNames(rs);

			//第二步，获取给定的这个类的所有属性名，但是类的属性是私有的，那么在这里是不能直接访问的，所以我们必须通过这个属性的setter方法来注值。所以我们直接获取这个类中的所有setter方法
			Method[] methods=c.getDeclaredMethods(); //获取这个类的所有方法
			//从这些方法中找出setter方法
			List<Method> setters=new ArrayList<Method>();
			for(Method method:methods){
				if(method.getName().startsWith("set")){
					setters.add(method);
				}
			}

			String mname=null;
			Class<?>[] types;
			String typeName=null;
			if(rs.next()){ //每循环一次就是一行数据，每一行数据就是一个对象
				t=c.newInstance();//先实例化一个给定的类类型的对象 StuInfo t=new StuInfo();

				//根据结果集中每个类的类名找到对应的setter，然后将值注入    
				for(String colName:colNames){ //sid sname
					mname="set"+colName;
					for(Method md:setters){ //setSid  setSname
						if(mname.equalsIgnoreCase(md.getName())){//sid-> setsid  sname->setsname
							//让这个方法运行一次，这个方法运行有两个要来，第一个这个方法是哪个对象的方法,第二个是这个方法的参数（此时这个方法的参数就是这个列的值）

							//问题：setter方法中的参数的类型不一定都是String类型的->这个时候我们可以使用强制类型转换，但前提条件是我必须这个你这个方法需要的参数类型
							types=md.getParameterTypes();
							//这个方法的形参类型是一个数组，而我们的标准setter方法只需要形参，所以我们只去这个数组中的第一个
							if(types!=null && types.length>0){
								typeName=types[0].getSimpleName(); //取出第一个形参的参数名

								if("int".equals(typeName) || "Integer".equals(typeName)){
									md.invoke(t,rs.getInt(colName)); //t.setSid(1001);
								}else if("float".equals(typeName) || "Float".equals(typeName)){
									md.invoke(t,rs.getFloat(colName));
								}else if("double".equals(typeName) || "Double".equals(typeName)){
									md.invoke(t,rs.getDouble(colName));
								}else{
									md.invoke(t,rs.getString(colName));
								}
							}

						}
					}
				}
			}
		} catch (SQLException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (InstantiationException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} finally {
			this.closeAll(rs, pstmt, con);
		}
		return t;
	}

	/**
	 * 查询
	 * @param sql 要执行的查询语句
	 * @param params 查询语句中占位符?对应的值
	 * @return 每一行记录存放到一个map中，将多行记录存放到list中
	 */
	public List<Map<String,String>> gets(String sql,List<Object> params){
		List<Map<String,String>> list=new ArrayList<Map<String,String>>();
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;


		try {
			con=this.getConnection();
			pstmt=con.prepareStatement(sql);

			//给占位符赋值
			this.setValue(pstmt, params);

			//获取结果集
			rs=pstmt.executeQuery();

			//获取结果中列的列名
			String[] colNames=this.getColumnNames(rs);

			Map<String,String> map=null;
			while(rs.next()){ //每循环一次就是一行数据，所以需要将这行数据存到map中，然后将这个map存到list中
				map=new HashMap<String,String>();
				//循环所有列的列名，根据列名取出对应列的值
				for(String colName:colNames){
					map.put(colName,rs.getString(colName));
				}
				list.add(map);
			}

		} catch (SQLException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} finally {
			this.closeAll(rs, pstmt, con);
		}
		return list;
	}

	/**
	 * 查询单行的方法
	 * @param sql
	 * @param params
	 * @return
	 */
	public Map<String,String> find(String sql,Object ... params){
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		Map<String,String> map=null;

		try {
			con=this.getConnection();
			pstmt=con.prepareStatement(sql);

			//给占位符赋值
			this.setValue(pstmt, params);

			//获取结果集
			rs=pstmt.executeQuery();

			//获取结果中列的列名
			String[] colNames=this.getColumnNames(rs);

			if(rs.next()){ //每循环一次就是一行数据，所以需要将这行数据存到map中，然后将这个map存到list中
				map=new HashMap<String,String>();
				//循环所有列的列名，根据列名取出对应列的值
				for(String colName:colNames){
					map.put(colName,rs.getString(colName));
				}
			}

		} catch (SQLException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} finally {
			this.closeAll(rs, pstmt, con);
		}
		return map;
	}

	/**
	 * @param sql
	 * @param params
	 * @return
	 */
	public Map<String,String> findScore(String sql,Object ... params){
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		Map<String,String> map=null;

		try {
			con=this.getConnection();
			pstmt=con.prepareStatement(sql);

			//给占位符赋值
			this.setValue(pstmt, params);

			//获取结果集
			rs=pstmt.executeQuery();

			map=new HashMap<String,String>();
			while(rs.next()){ //每循环一次就是一行数据，所以需要将这行数据存到map中，然后将这个map存到list中
				//循环所有列的列名，根据列名取出对应列的值
				map.put(rs.getString("SNAME"),rs.getString("SCORE"));
			}
		} catch (SQLException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} finally {
			this.closeAll(rs, pstmt, con);
		}
		return map;
	}

	/**
	 * 基于对象的查询
	 * @param sql 要执行的sql语句
	 * @param c 这个语句对应的实体类
	 * @param params 要执行的sql语句中占位符?的值
	 * @return
	 */
	public <T> List<T> finds(String sql,Class<T> c,Object ... params){
		List<T> list=new ArrayList<T>();
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;

		try {
			con=this.getConnection();
			pstmt=con.prepareStatement(sql);

			//给占位符赋值
			this.setValue(pstmt, params);

			//获取结果集
			rs=pstmt.executeQuery();



			//必须提前约定，属性名必须跟结果集中的列名相同。如果不一致，则我们就不知道这个列的值应该注入到对象的哪个属性中。如果列名跟属性名不同时，我们可以使用列的重命名方式，将列名重命名为对应的属性名

			//操作步骤，第一步必须获取这个结果集中所有的列的列名
			String[] colNames=this.getColumnNames(rs);

			//第二步，获取给定的这个类的所有属性名，但是类的属性是私有的，那么在这里是不能直接访问的，所以我们必须通过这个属性的setter方法来注值。所以我们直接获取这个类中的所有setter方法
			Method[] methods=c.getDeclaredMethods(); //获取这个类的所有方法
			//从这些方法中找出setter方法
			List<Method> setters=new ArrayList<Method>();
			for(Method method:methods){
				if(method.getName().startsWith("set")){
					setters.add(method);
				}
			}

			T t=null;
			String mname=null;
			Class<?>[] types;
			String typeName=null;
			while(rs.next()){ //每循环一次就是一行数据，每一行数据就是一个对象
				t=c.newInstance();//先实例化一个给定的类类型的对象 StuInfo t=new StuInfo();

				//根据结果集中每个类的类名找到对应的setter，然后将值注入    
				for(String colName:colNames){ //sid sname
					mname="set"+colName;
					for(Method md:setters){ //setSid  setSname
						if(mname.equalsIgnoreCase(md.getName())){//sid-> setsid  sname->setsname
							//让这个方法运行一次，这个方法运行有两个要来，第一个这个方法是哪个对象的方法,第二个是这个方法的参数（此时这个方法的参数就是这个列的值）

							//问题：setter方法中的参数的类型不一定都是String类型的->这个时候我们可以使用强制类型转换，但前提条件是我必须这个你这个方法需要的参数类型
							types=md.getParameterTypes();
							//这个方法的形参类型是一个数组，而我们的标准setter方法只需要形参，所以我们只去这个数组中的第一个
							if(types!=null && types.length>0){
								typeName=types[0].getSimpleName(); //取出第一个形参的参数名

								if("int".equals(typeName) || "Integer".equals(typeName)){
									md.invoke(t,rs.getInt(colName)); //t.setSid(1001);
								}else if("float".equals(typeName) || "Float".equals(typeName)){
									md.invoke(t,rs.getFloat(colName));
								}else if("double".equals(typeName) || "Double".equals(typeName)){
									md.invoke(t,rs.getDouble(colName));
								}else{
									md.invoke(t,rs.getString(colName));
								}
							}

						}
					}
				}
				list.add(t);
			}

		} catch (SQLException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (InstantiationException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} finally {
			this.closeAll(rs, pstmt, con);
		}
		return list;
	}

	/**
	 * 基于对象的查询
	 * @param sql 要执行的sql语句
	 * @param c 这个语句对应的实体类
	 * @param params 要执行的sql语句中占位符?的值
	 * @return
	 */
	public <T> T find(String sql,Class<T> c,Object ... params){
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		T t=null;
		try {
			con=this.getConnection();
			pstmt=con.prepareStatement(sql);

			//给占位符赋值
			this.setValue(pstmt, params);

			//获取结果集
			rs=pstmt.executeQuery();



			//必须提前约定，属性名必须跟结果集中的列名相同。如果不一致，则我们就不知道这个列的值应该注入到对象的哪个属性中。如果列名跟属性名不同时，我们可以使用列的重命名方式，将列名重命名为对应的属性名

			//操作步骤，第一步必须获取这个结果集中所有的列的列名
			String[] colNames=this.getColumnNames(rs);

			//第二步，获取给定的这个类的所有属性名，但是类的属性是私有的，那么在这里是不能直接访问的，所以我们必须通过这个属性的setter方法来注值。所以我们直接获取这个类中的所有setter方法
			Method[] methods=c.getDeclaredMethods(); //获取这个类的所有方法
			//从这些方法中找出setter方法
			List<Method> setters=new ArrayList<Method>();
			for(Method method:methods){
				if(method.getName().startsWith("set")){
					setters.add(method);
				}
			}

			String mname=null;
			Class<?>[] types;
			String typeName=null;
			if(rs.next()){ //每循环一次就是一行数据，每一行数据就是一个对象
				t=c.newInstance();//先实例化一个给定的类类型的对象 StuInfo t=new StuInfo();

				//根据结果集中每个类的类名找到对应的setter，然后将值注入    
				for(String colName:colNames){ //sid sname
					mname="set"+colName;
					for(Method md:setters){ //setSid  setSname
						if(mname.equalsIgnoreCase(md.getName())){//sid-> setsid  sname->setsname
							//让这个方法运行一次，这个方法运行有两个要来，第一个这个方法是哪个对象的方法,第二个是这个方法的参数（此时这个方法的参数就是这个列的值）

							//问题：setter方法中的参数的类型不一定都是String类型的->这个时候我们可以使用强制类型转换，但前提条件是我必须这个你这个方法需要的参数类型
							types=md.getParameterTypes();
							//这个方法的形参类型是一个数组，而我们的标准setter方法只需要形参，所以我们只去这个数组中的第一个
							if(types!=null && types.length>0){
								typeName=types[0].getSimpleName(); //取出第一个形参的参数名

								if("int".equals(typeName) || "Integer".equals(typeName)){
									md.invoke(t,rs.getInt(colName)); //t.setSid(1001);
								}else if("float".equals(typeName) || "Float".equals(typeName)){
									md.invoke(t,rs.getFloat(colName));
								}else if("double".equals(typeName) || "Double".equals(typeName)){
									md.invoke(t,rs.getDouble(colName));
								}else{
									md.invoke(t,rs.getString(colName));
								}
							}

						}
					}
				}
			}
		} catch (SQLException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (InstantiationException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} finally {
			this.closeAll(rs, pstmt, con);
		}
		return t;
	}
	/**
	 * 基于对象的查询
	 * @param sql 要执行的sql语句
	 * @param c 这个语句对应的实体类
	 * @param params 要执行的sql语句中占位符?的值
	 * @return
	 */
	public <T> List<T> finds(String sql,Class<T> c,List<Object> params){
		List<T> list=new ArrayList<T>();
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;

		try {
			con=this.getConnection();
			pstmt=con.prepareStatement(sql);

			//给占位符赋值
			this.setValue(pstmt, params);

			//获取结果集
			rs=pstmt.executeQuery();



			//必须提前约定，属性名必须跟结果集中的列名相同。如果不一致，则我们就不知道这个列的值应该注入到对象的哪个属性中。如果列名跟属性名不同时，我们可以使用列的重命名方式，将列名重命名为对应的属性名

			//操作步骤，第一步必须获取这个结果集中所有的列的列名
			String[] colNames=this.getColumnNames(rs);

			//第二步，获取给定的这个类的所有属性名，但是类的属性是私有的，那么在这里是不能直接访问的，所以我们必须通过这个属性的setter方法来注值。所以我们直接获取这个类中的所有setter方法
			Method[] methods=c.getDeclaredMethods(); //获取这个类的所有方法
			//从这些方法中找出setter方法
			List<Method> setters=new ArrayList<Method>();
			for(Method method:methods){
				if(method.getName().startsWith("set")){
					setters.add(method);
				}
			}

			T t=null;
			String mname=null;
			Class<?>[] types;
			String typeName=null;
			while(rs.next()){ //每循环一次就是一行数据，每一行数据就是一个对象
				t=c.newInstance();//先实例化一个给定的类类型的对象 StuInfo t=new StuInfo();

				//根据结果集中每个类的类名找到对应的setter，然后将值注入    
				for(String colName:colNames){ //sid sname
					mname="set"+colName;
					for(Method md:setters){ //setSid  setSname
						if(mname.equalsIgnoreCase(md.getName())){//sid-> setsid  sname->setsname
							//让这个方法运行一次，这个方法运行有两个要来，第一个这个方法是哪个对象的方法,第二个是这个方法的参数（此时这个方法的参数就是这个列的值）

							//问题：setter方法中的参数的类型不一定都是String类型的->这个时候我们可以使用强制类型转换，但前提条件是我必须这个你这个方法需要的参数类型
							types=md.getParameterTypes();
							//这个方法的形参类型是一个数组，而我们的标准setter方法只需要形参，所以我们只去这个数组中的第一个
							if(types!=null && types.length>0){
								typeName=types[0].getSimpleName(); //取出第一个形参的参数名

								if("int".equals(typeName) || "Integer".equals(typeName)){
									md.invoke(t,rs.getInt(colName)); //t.setSid(1001);
								}else if("float".equals(typeName) || "Float".equals(typeName)){
									md.invoke(t,rs.getFloat(colName));
								}else if("double".equals(typeName) || "Double".equals(typeName)){
									md.invoke(t,rs.getDouble(colName));
								}else{
									md.invoke(t,rs.getString(colName));
								}
							}

						}
					}
				}
				list.add(t);
			}

		} catch (SQLException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (InstantiationException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} finally {
			this.closeAll(rs, pstmt, con);
		}
		return list;
	}

	/**
	 * 基于对象的查询
	 * @param sql 要执行的sql语句
	 * @param c 这个语句对应的实体类
	 * @param params 要执行的sql语句中占位符?的值
	 * @return
	 */
	public <T> T find(String sql,Class<T> c,List<Object> params){
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		T t=null;
		try {
			con=this.getConnection();
			pstmt=con.prepareStatement(sql);

			//给占位符赋值
			this.setValue(pstmt, params);

			//获取结果集
			rs=pstmt.executeQuery();



			//必须提前约定，属性名必须跟结果集中的列名相同。如果不一致，则我们就不知道这个列的值应该注入到对象的哪个属性中。如果列名跟属性名不同时，我们可以使用列的重命名方式，将列名重命名为对应的属性名

			//操作步骤，第一步必须获取这个结果集中所有的列的列名
			String[] colNames=this.getColumnNames(rs);

			//第二步，获取给定的这个类的所有属性名，但是类的属性是私有的，那么在这里是不能直接访问的，所以我们必须通过这个属性的setter方法来注值。所以我们直接获取这个类中的所有setter方法
			Method[] methods=c.getDeclaredMethods(); //获取这个类的所有方法
			//从这些方法中找出setter方法
			List<Method> setters=new ArrayList<Method>();
			for(Method method:methods){
				if(method.getName().startsWith("set")){
					setters.add(method);
				}
			}

			String mname=null;
			Class<?>[] types;
			String typeName=null;
			if(rs.next()){ //每循环一次就是一行数据，每一行数据就是一个对象
				t=c.newInstance();//先实例化一个给定的类类型的对象 StuInfo t=new StuInfo();

				//根据结果集中每个类的类名找到对应的setter，然后将值注入    
				for(String colName:colNames){ //sid sname
					mname="set"+colName;
					for(Method md:setters){ //setSid  setSname
						if(mname.equalsIgnoreCase(md.getName())){//sid-> setsid  sname->setsname
							//让这个方法运行一次，这个方法运行有两个要来，第一个这个方法是哪个对象的方法,第二个是这个方法的参数（此时这个方法的参数就是这个列的值）

							//问题：setter方法中的参数的类型不一定都是String类型的->这个时候我们可以使用强制类型转换，但前提条件是我必须这个你这个方法需要的参数类型
							types=md.getParameterTypes();
							//这个方法的形参类型是一个数组，而我们的标准setter方法只需要形参，所以我们只去这个数组中的第一个
							if(types!=null && types.length>0){
								typeName=types[0].getSimpleName(); //取出第一个形参的参数名

								if("int".equals(typeName) || "Integer".equals(typeName)){
									md.invoke(t,rs.getInt(colName)); //t.setSid(1001);
								}else if("float".equals(typeName) || "Float".equals(typeName)){
									md.invoke(t,rs.getFloat(colName));
								}else if("double".equals(typeName) || "Double".equals(typeName)){
									md.invoke(t,rs.getDouble(colName));
								}else{
									md.invoke(t,rs.getString(colName));
								}
							}

						}
					}
				}
			}
		} catch (SQLException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (InstantiationException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} finally {
			this.closeAll(rs, pstmt, con);
		}
		return t;
	}

	/**
	 *  获取总记录数的方法
	 * @param sql
	 * @param params
	 * @return
	 */
	public int getTotal(String sql,Object ... params){
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		try {
			con=this.getConnection();
			pstmt=con.prepareStatement(sql);

			//给占位符赋值
			this.setValue(pstmt, params);

			//获取结果集
			rs=pstmt.executeQuery();


			if(rs.next()){ //每循环一次就是一行数据，所以需要将这行数据存到map中，然后将这个map存到list中
				//循环所有列的列名，根据列名取出对应列的值
				return rs.getInt(1);
			}

		} catch (SQLException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} finally {
			this.closeAll(rs, pstmt, con);
		}
		return 0;
	}

	/**
	 * 获取总记录数的方法
	 * @param sql
	 * @param params
	 * @return
	 */
	public int getTotal(String sql,List<Object> params){
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		try {
			con=this.getConnection();
			pstmt=con.prepareStatement(sql);

			//给占位符赋值
			this.setValue(pstmt, params);

			//获取结果集
			rs=pstmt.executeQuery();


			if(rs.next()){ //每循环一次就是一行数据，所以需要将这行数据存到map中，然后将这个map存到list中
				//循环所有列的列名，根据列名取出对应列的值
				return rs.getInt(1);
			}

		} catch (SQLException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} finally {
			this.closeAll(rs, pstmt, con);
		}
		return 0;
	}

	/**
	 * 批量添加
	 * @param sql
	 * @param list
	 * @return
	 */
	public int updates(String sql,List<List<String>> params){
		Connection con=null;
		PreparedStatement pstmt=null;
		int[] result = null;
		try {
			con=this.getConnection();
			con.setAutoCommit(false);
			pstmt=con.prepareStatement(sql);
			for(List<String> list : params){
				this.setValues(pstmt,list);
				pstmt.addBatch();  // 加入到批处理中
			}
			result = pstmt.executeBatch();  // 执行批处理
			con.commit();

			if(result != null){
				return result.length;
			}
			return 0;
		}  catch (SQLException e) {
			LogUtil.log.error(e);
			e.printStackTrace();
		} finally{
			try {
				con.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			this.closeAll(null, pstmt, con);
		}
		return 0;
	}
}
