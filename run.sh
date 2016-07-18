#!/bin/bash
java -cp "./api/target/test-classes:" -jar `find api/target/ -name "api*jar"` 
