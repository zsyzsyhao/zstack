package org.zstack.core.aspect;

/**
 */
public aspect AspectOrder {
    declare precedence : ExceptionSafeAspect, ThreadAspect, MessageSafeAspect, AsyncSafeAspect, AsyncBackupAspect,
            CompletionSingleCallAspect, EncryptAspect, DbDeadlockAspect, DeferAspect, AnnotationTransactionAspect, UnitTestBypassMethodAspect;
}
