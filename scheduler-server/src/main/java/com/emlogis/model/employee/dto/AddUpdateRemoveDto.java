package com.emlogis.model.employee.dto;

import java.util.Collection;

public class AddUpdateRemoveDto<T> {

    private Collection<T> addCollection;
    private Collection<String> removeCollection;
    private Collection<T> updateCollection;

    public Collection<T> getAddCollection() {
        return addCollection;
    }

    public void setAddCollection(Collection<T> addCollection) {
        this.addCollection = addCollection;
    }

    public Collection<String> getRemoveCollection() {
        return removeCollection;
    }

    public void setRemoveCollection(Collection<String> removeCollection) {
        this.removeCollection = removeCollection;
    }

    public Collection<T> getUpdateCollection() {
        return updateCollection;
    }

    public void setUpdateCollection(Collection<T> updateCollection) {
        this.updateCollection = updateCollection;
    }
}
