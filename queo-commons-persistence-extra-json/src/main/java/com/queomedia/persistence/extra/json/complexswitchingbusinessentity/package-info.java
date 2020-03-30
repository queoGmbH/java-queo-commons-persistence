/**
 * This package contains a Json Module with serializer and deserializer (as well as KeySerializer/Deserializer) for
 * {@link com.queomedia.persistence.BusinessEntity}s that can either serialized as plain entity or just with there
 * BusinessID.
 * The switch between this two serialization modes can be controlled by
 * {@link com.queomedia.persistence.extra.json.SwitchingBusinessEntityAnnotation}.
 * A annotation that can be placed by any parent class in the json-serialization path.
 *
 * <p>
 * Functionality is activated by registration of jackson module
 * {@link com.queomedia.persistence.extra.json.complexswitchingbusinessentity.ComplexSwitchingBusinessEntityModule}.
 * This module activate, the switching serializer and deserializer as well as a KeySerializer/Deserializer that
 * always serialize just the Business Id.
 * </p>
 *
 * <p>
 * The {@link com.queomedia.persistence.extra.json.SwitchingBusinessEntityAnnotation} defined
 * how the business entities are serialized. The annotation is picked up by
 * {@link com.queomedia.persistence.extra.json.SwitchingAnnotationScanner} which is invoked by
 * the serializers and deserializers when they needs to serialize/deserialize an
 * {@link com.queomedia.persistence.BusinessEntity}.
 * </p>
 */
package com.queomedia.persistence.extra.json.complexswitchingbusinessentity;
