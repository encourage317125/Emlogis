package com.emlogis.workflow.api.crypt;

import com.emlogis.model.employee.Employee;
import com.emlogis.workflow.exception.WorkflowServerException;

import java.io.UnsupportedEncodingException;

/**
 * Created by alex on 3/23/15.
 */
public interface ICryptoService {

    byte[] encodeUrl(Employee employee, String uuid) throws WorkflowServerException;

    String decode(Employee employee, String url) throws WorkflowServerException;

    byte [] encrypt(String passphrase, String plaintext) throws UnsupportedEncodingException;

    String decrypt(String passphrase, byte [] ciphertext);
}
