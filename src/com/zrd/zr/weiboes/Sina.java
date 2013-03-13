package com.zrd.zr.weiboes;

import java.io.Serializable;

import weibo4android.User;
import weibo4android.Weibo;
import weibo4android.OAuthConstant;

public class Sina implements Serializable {
	
	/**
	 * generated by system 
	 */
	private static final long serialVersionUID = 7590823845763389527L;

	private Weibo mWeibo = null;
	
	/*
	 * when some account of SINA_weibo actually logged in,
	 * we told every activities that they should open some
	 * rights for it.
	 */
	private User mLoggedInUser = null;
	
	/*
	 * implement "tag"
	 */
	private Object mTag = null;
	
	public Object getTag() {
		return mTag;
	}

	public void setTag(Object tag) {
		this.mTag = tag;
	}

	public Sina() {
		this(false);
	}
	
	public Sina(boolean noDefaultAccessToken) {
		mWeibo = OAuthConstant.getInstance().getWeibo();
		
		/*
		 * the necessary key/secret pair that needed when
		 * try to using the API of SINA_weibo
		 */
		mWeibo.setOAuthConsumer(
			Weibo.CONSUMER_KEY,
			Weibo.CONSUMER_SECRET
		);
		
		if (noDefaultAccessToken) {
			mWeibo.setOAuthAccessToken("", "");
		} else {
			/*
			 * we use the following tokens for default using,
			 * but don't allow further more actions, like submit
			 * a SINA_weibo or something like that.
			 */
			mWeibo.setOAuthAccessToken(
				Weibo.ANFFERNEE_TOKEN, 
				Weibo.ANFFERNEE_TOKEN_SECRET
			);
		}
	}
	
	public Weibo getWeibo() {
		return mWeibo;
	}

	public void setLoggedInUser(User user) {
		mLoggedInUser = user;
	}
	
	public User getLoggedInUser() {
		return mLoggedInUser;
	}
	
	public boolean isLoggedIn() {
		return (mLoggedInUser != null);
	}
	
	public XStatus getXStatus() {
		return new XStatus();
	}
	
	public class XStatus implements Serializable {
		/**
		 * just in order to record the comments & reposts
		 * for the original status
		 */
		private static final long serialVersionUID = -4063554782772695640L;
		private weibo4android.Status status;
		private long comments = 0;
		private long reposts = 0;
		public void setStatus(weibo4android.Status status) {
			this.status = status;
		}
		public weibo4android.Status getStatus() {
			return status;
		}
		public void setComments(long comments) {
			this.comments = comments;
		}
		public long getComments() {
			return comments;
		}
		public void setReposts(long reposts) {
			this.reposts = reposts;
		}
		public long getReposts() {
			return reposts;
		}
	}
		
}
