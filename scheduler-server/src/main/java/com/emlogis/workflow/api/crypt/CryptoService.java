package com.emlogis.workflow.api.crypt;

import com.amazonaws.util.Base64;
import com.emlogis.model.employee.Employee;
import com.emlogis.workflow.exception.WorkflowServerException;

import javax.ejb.Local;
import javax.ejb.Stateless;
import java.io.UnsupportedEncodingException;

import static com.emlogis.workflow.WflUtil.UTF_8;
import static com.emlogis.workflow.exception.ExceptionCode.CAN_NOT_DECODE_REQUEST_NOTIFICATION;
import static com.emlogis.workflow.exception.ExceptionCode.CAN_NOT_ENCODE_REQUEST_NOTIFICATION;

/**
 * Created by alex on 3/23/15.
 */
@Stateless
@Local(ICryptoService.class)
public class CryptoService implements ICryptoService {

    public byte[] encrypt(String passphrase, String plaintext) throws UnsupportedEncodingException {
        return Base64.encode(plaintext.getBytes("UTF-8"));
    }

    public String decrypt(String passphrase, byte[] ciphertext) {
        return new String(Base64.decode(ciphertext));
    }

    public byte[] encodeUrl(Employee employee, String uuid) throws WorkflowServerException {
        try {
            return encrypt(employee.getId(), uuid);
        } catch (Exception error) {
            throw new WorkflowServerException(CAN_NOT_ENCODE_REQUEST_NOTIFICATION, "Can not decrypt this URL", error);
        }
    }

    public String decode(Employee employee, String url) throws WorkflowServerException {
        try {
            return decrypt(employee.getId(), url.getBytes(UTF_8));
        } catch (Exception error) {
            throw new WorkflowServerException(CAN_NOT_DECODE_REQUEST_NOTIFICATION, "Can not decrypt this URL", error);
        }
    }

}
