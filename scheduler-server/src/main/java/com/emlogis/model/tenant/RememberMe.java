package com.emlogis.model.tenant;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity
@Table(indexes = {
		@Index(name = "REMEMBERME__CLIENTID_INDEX", unique = false, columnList = "clientUniqueId") ,
		@Index(name = "REMEMBERME__TOKENID_INDEX", unique = false, columnList = "tokenId")
})
public class RememberMe extends BaseEntity {
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "userAccountTenantId", referencedColumnName = "tenantId"),
            @JoinColumn(name = "userAccountId", referencedColumnName = "id")
    })
    private UserAccount userAccount;

    private	String clientUniqueId;		// should be a unique identifier like a device/mobile phone id
    
    private	String clientDescr;		// user friendly description of

    private	String tokenId;			// token associated to current session
    
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime expirationDate = new DateTime(0);    // date of expiration for that 'client'

    
    public RememberMe() {}

    public RememberMe(PrimaryKey primaryKey) {
        super(primaryKey);
    }

    public UserAccount getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(UserAccount userAccount) {
		this.userAccount = userAccount;
	}

	public String getClientUniqueId() {
		return clientUniqueId;
	}

	public void setClientUniqueId(String clientUniqueId) {
		this.clientUniqueId = clientUniqueId;
	}

	public String getClientDescr() {
		return clientDescr;
	}

	public void setClientDescr(String clientDescr) {
		this.clientDescr = clientDescr;
	}

	public long getExpirationDate() {
		return expirationDate.getMillis();
	}

	public void setExpirationDate(DateTime expirationDate) {
		this.expirationDate = expirationDate;
	}
	
	public void setExpirationDate(long expirationDate) {
		this.expirationDate = new DateTime(expirationDate);
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}
	
}
