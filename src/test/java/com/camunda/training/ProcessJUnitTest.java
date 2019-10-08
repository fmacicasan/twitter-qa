package com.camunda.training;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.assertj.core.api.Assertions.*;

public class ProcessJUnitTest {
    @Rule @ClassRule
    //public ProcessEngineRule rule = new ProcessEngineRule();
    public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();
    @Before
    public void setup() {
        init(rule.getProcessEngine());
        Mocks.register("publishTweetDelegate", new FakeDelegate());
    }

    @Test
    @Deployment(resources = "TwitterQA.bpmn")
    public void testHappyPath() {
        // Create a HashMap to put in variables for the process instance
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("content", "Where is my cattt?");
        // Start process with Java API and variables
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("TwitterQAProcess", variables);
        // Make assertions on the process instance
        assertThat(processInstance).isWaitingAt("CheckTweetTask");
        List<Task> tasks = taskService().createTaskQuery().list();
        assertThat(tasks.get(0)).hasCandidateGroup("management");
        taskService().complete(tasks.get(0).getId(), withVariables("approved", true));
        assertThat(processInstance).isEnded();
    }

    @Test
    @Deployment(resources = "TwitterQA.bpmn")
    public void testNotApprovedPath() {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("approved", false);
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("TwitterQAProcess", variables);
        assertThat(processInstance).isWaitingAt("CheckTweetTask");
        taskService().complete(task().getId());
        assertThat(processInstance).isEnded();
    }

}
