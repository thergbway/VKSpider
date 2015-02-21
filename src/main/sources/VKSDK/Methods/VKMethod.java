package VKSDK.Methods;

import VKSDK.Exceptions.VKException;

public interface VKMethod {
    Object invoke() throws VKException;
}
