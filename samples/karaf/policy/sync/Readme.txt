Synchronous Policy Enforcement
==============================

This quickstart provides an example of a business policy that has been defined to run synchronously
within the execution environment. The decisions taken by this policy are immediately actionable, allowing
them to be used to block the business transaction. In this specific example, if the customer issues multiple
requests too quickly, the requests will be rejected, so providing a crude form of throttling.

For this quickstart to function correctly, you must also deploy the Order Management Application and
Information Processor.

To deploy the quickstart, after the console has been started, run:

	features:install rtgov-samples-policy-sync

To undeploy the quickstart, run:

	features:uninstall rtgov-samples-policy-sync


NOTE: This quickstart can be deployed with the feature(s): rtgov-client or rtgov-all

