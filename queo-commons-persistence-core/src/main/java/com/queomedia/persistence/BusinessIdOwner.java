package com.queomedia.persistence;

import java.io.Serializable;

public interface BusinessIdOwner<T extends Serializable> {

    BusinessId<T> getBusinessId();
}
