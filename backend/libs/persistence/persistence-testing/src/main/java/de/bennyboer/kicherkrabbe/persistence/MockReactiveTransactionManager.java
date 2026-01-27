package de.bennyboer.kicherkrabbe.persistence;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import reactor.core.publisher.Mono;

import static lombok.AccessLevel.PRIVATE;

public class MockReactiveTransactionManager implements ReactiveTransactionManager {

    @Override
    public Mono<ReactiveTransaction> getReactiveTransaction(TransactionDefinition definition) throws
            TransactionException {
        return Mono.fromCallable(() -> {
            boolean isActualTransactionActiveBefore = TransactionSynchronizationManager.isActualTransactionActive();
            TransactionSynchronizationManager.setActualTransactionActive(true);

            return MockReactiveTransaction.of(isActualTransactionActiveBefore);
        });
    }

    @Override
    public Mono<Void> commit(ReactiveTransaction transaction) throws TransactionException {
        if (transaction instanceof MockReactiveTransaction mockReactiveTransaction) {
            return Mono.fromCallable(() -> {
                TransactionSynchronizationManager.setActualTransactionActive(mockReactiveTransaction.isActualTransactionActiveBefore());
                return null;
            });
        }

        return Mono.empty();
    }

    @Override
    public Mono<Void> rollback(ReactiveTransaction transaction) throws TransactionException {
        if (transaction instanceof MockReactiveTransaction mockReactiveTransaction) {
            return Mono.fromCallable(() -> {
                TransactionSynchronizationManager.setActualTransactionActive(mockReactiveTransaction.isActualTransactionActiveBefore());
                return null;
            });
        }

        return Mono.empty();
    }

    @Value
    @AllArgsConstructor(access = PRIVATE)
    public static class MockReactiveTransaction implements ReactiveTransaction {

        boolean actualTransactionActiveBefore;

        public static MockReactiveTransaction of(boolean actualTransactionActiveBefore) {
            return new MockReactiveTransaction(actualTransactionActiveBefore);
        }

        @Override
        public boolean isNewTransaction() {
            return true;
        }

        @Override
        public void setRollbackOnly() {
        }

        @Override
        public boolean isRollbackOnly() {
            return false;
        }

        @Override
        public boolean isCompleted() {
            return false;
        }

    }

}
