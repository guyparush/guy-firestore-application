package com.katza.guyfirstfbapplication;

public interface IFFirestoreImageCallback<T> {

    void onSuccess(T result);

    void onFailure(Exception error);
}
