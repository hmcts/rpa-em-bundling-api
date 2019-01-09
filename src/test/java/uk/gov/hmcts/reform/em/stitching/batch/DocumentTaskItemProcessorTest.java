package uk.gov.hmcts.reform.em.stitching.batch;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.em.stitching.Application;
import uk.gov.hmcts.reform.em.stitching.domain.DocumentTask;
import uk.gov.hmcts.reform.em.stitching.domain.enumeration.TaskState;
import uk.gov.hmcts.reform.em.stitching.service.DmStoreDownloader;
import uk.gov.hmcts.reform.em.stitching.service.impl.DocumentTaskProcessingException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@Transactional
public class DocumentTaskItemProcessorTest {

    @Mock
    DmStoreDownloader dmStoreDownloader;

    @Test
    public void testFailure() throws Exception {

        String docId = "X";

        DocumentTask documentTask = new DocumentTask();
        documentTask.setBundle(docId);

        Mockito.when(dmStoreDownloader.downloadFile(docId)).thenThrow(new DocumentTaskProcessingException("problem"));

        DocumentTaskItemProcessor documentTaskItemProcessor = new DocumentTaskItemProcessor(dmStoreDownloader, null, null, null);

        documentTaskItemProcessor.process(documentTask);

        Assert.assertEquals("problem", documentTask.getFailureDescription());
        Assert.assertEquals(TaskState.FAILED, documentTask.getTaskState());
    }

}