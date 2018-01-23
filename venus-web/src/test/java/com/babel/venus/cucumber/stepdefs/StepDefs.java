package com.babel.venus.cucumber.stepdefs;

import com.babel.venus.VenusApp;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import org.springframework.boot.test.context.SpringBootTest;

@WebAppConfiguration
@SpringBootTest
@ContextConfiguration(classes = VenusApp.class)
public abstract class StepDefs {

    protected ResultActions actions;

}
