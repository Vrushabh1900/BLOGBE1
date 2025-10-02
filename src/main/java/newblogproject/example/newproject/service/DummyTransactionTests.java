package newblogproject.example.newproject.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@NoArgsConstructor
@AllArgsConstructor
public class DummyTransactionTests {
    private TransactionTemplate TT;
    private PlatformTransactionManager ptm;
    public void dosm()
    {TransactionCallback<TransactionStatus> TC =(TransactionStatus status)->{
        return status;
    };
   TT.execute(TC);
    }

    public void dosmagain()
    {
        TransactionStatus status= ptm.getTransaction(null);
        try{
            ptm.commit(status);
        } catch (Exception e) {
            ptm.rollback(status);
        }
    }

}
