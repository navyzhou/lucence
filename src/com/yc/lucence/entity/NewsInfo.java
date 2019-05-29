package com.yc.lucence.entity;

public class NewsInfo {
	private int nid;
	private String title;
	private String ndate;
	private String author;
	private String content;

	@Override
	public String toString() {
		return "NewsInfo [nid=" + nid + ", title=" + title + ", ndate=" + ndate + ", author=" + author + ", content="
				+ content + "]";
	}

	public int getNid() {
		return nid;
	}

	public void setNid(int nid) {
		this.nid = nid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getNdate() {
		return ndate;
	}

	public void setNdate(String ndate) {
		this.ndate = ndate;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}


	public NewsInfo(int nid, String title, String ndate, String author, String content) {
		super();
		this.nid = nid;
		this.title = title;
		this.ndate = ndate;
		this.author = author;
		this.content = content;
	}

	public NewsInfo() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((ndate == null) ? 0 : ndate.hashCode());
		result = prime * result + nid;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NewsInfo other = (NewsInfo) obj;
		if (author == null) {
			if (other.author != null)
				return false;
		} else if (!author.equals(other.author))
			return false;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (ndate == null) {
			if (other.ndate != null)
				return false;
		} else if (!ndate.equals(other.ndate))
			return false;
		if (nid != other.nid)
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
}
