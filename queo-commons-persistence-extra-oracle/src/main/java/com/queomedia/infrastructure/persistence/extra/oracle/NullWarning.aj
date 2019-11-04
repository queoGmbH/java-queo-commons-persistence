//package com.queomedia.infrastructure.persistence;
//
//import javax.persistence.Entity;
//import javax.persistence.Embedded;
//import javax.persistence.Enumerated;
//import javax.validation.constraints.NotNull;
//
//import org.hibernate.validator.constraints.NotBlank;
//import org.hibernate.validator.constraints.NotEmpty;
//import org.hibernate.annotations.Type;
//
///** 
// * Use this aspect to find all the usage of fields that maybe require a 
// * @Type(type = "com.queomedia.infrastructure.persistence.NotNullString") annotation.
// * 
// * or should use a @NotNullExceptForOracle instead of @NotNull
// * 
// * This aspect is out commented, because it is slow!
// * 
// * @author Ralph Engelmann
// */
//public aspect NullWarning {
//
//    pointcut requiresTypeAnnotation(): (within(@Entity *) || within(@Embedded *)) && 
//        (
//                (get(@NotNull private java.lang.String *)
//                ||get(@NotBlank private java.lang.String *)
//                ||get(@NotEmpty private java.lang.String *)
//                ||get(@NotNullExceptForOracle private java.lang.String *)
//                )&& 
//                !get(@Enumerated private java.lang.String *)
//                &&
//                !get(@Type private java.lang.String *));
//    
//    
//    declare warning : requiresTypeAnnotation() : "expect: @Type(type = \"com.queomedia.infrastructure.persistence.NotNullString\")";
//    
//    
//    pointcut noNotNullRecommend(): (within(@Entity *) || within(@Embedded *)) && 
//    (
//            get(@NotNull private java.lang.String *)
//            && 
//            !get(@Enumerated private java.lang.String *));
//    
//
//    declare warning : noNotNullRecommend() : "use @NotNullExceptForOracle instead of @NotNull";
//}
//
