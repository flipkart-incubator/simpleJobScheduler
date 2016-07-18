#!/bin/bash
set -e
set -x
echo "drop database if exists http_job_scheduler" | mysql -uroot
echo "create database http_job_scheduler" | mysql -uroot
