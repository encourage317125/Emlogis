package com.emlogis.model.tenant;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

@Entity
public class AccountPicture extends BaseEntity {

    @OneToOne()
    private UserAccount userAccount;

    @Lob
    private byte[] image;

    public AccountPicture() {}

    public AccountPicture(PrimaryKey primaryKey) {
        super(primaryKey);
    }

    public UserAccount getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(UserAccount userAccount) {
		this.userAccount = userAccount;
	}

	public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
