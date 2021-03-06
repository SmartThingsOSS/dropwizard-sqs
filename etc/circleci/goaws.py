#!/usr/bin/env python
from __future__ import print_function

import boto3
import argparse

def run(region, endpoint_url):
    sns = boto3.client('sns', region_name=region, endpoint_url=endpoint_url)
    sqs = boto3.client('sqs', region_name=region, endpoint_url=endpoint_url)

    print('Creating sqs queues')
    queue_response_1 = sqs.create_queue(QueueName='functional_test_queue')
    queue_response_2 = sqs.create_queue(QueueName='simple_test_queue')
    print('queue_response_1 = {0}'.format(queue_response_1))
    print('queue_response_2 = {0}'.format(queue_response_2))

    print('Creating sns topics...')
    topic_response_1 = sns.create_topic(Name='functional_test_queue')
    print('Created sns topic {0}'.format(topic_response_1['TopicArn']))

    print('Creating sns subscriptions...')
    sns.subscribe(
        TopicArn=topic_response_1['TopicArn'],
        Protocol='sqs',
        Endpoint='{0}/queue/functional_test_queue'.format(endpoint_url)
    )

    print('Completed SNS/SQS setup.')

def main():
    p = argparse.ArgumentParser()
    p.add_argument('--region', default='us-east-1', help='What region?')
    p.add_argument('--endpoint-url', default='http://goaws:4100', help='What Endpoint URL?')
    args = p.parse_args()
    run(args.region, args.endpoint_url)

if __name__ == '__main__':
    main()