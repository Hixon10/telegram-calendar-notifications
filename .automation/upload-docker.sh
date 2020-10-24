GCR_USERNAME="${GCR_USERNAME}"            # Username to login to GitHub package registry
GCR_TOKEN="${GCR_TOKEN}"                  # Password to login to GitHub package registry
GITHUB_SHA="${GITHUB_SHA}"                # The commit SHA that triggered the workflow
PRJ_NAME='telegram-calendar-notifications'

echo $GCR_TOKEN | docker login ghcr.io --username $GCR_USERNAME --password-stdin

docker tag $PRJ_NAME:latest ghcr.io/hixon10/$PRJ_NAME/$PRJ_NAME:$GITHUB_SHA
docker tag $PRJ_NAME:latest ghcr.io/hixon10/$PRJ_NAME/$PRJ_NAME:latest

docker push ghcr.io/hixon10/$PRJ_NAME/$PRJ_NAME:$GITHUB_SHA
docker push ghcr.io/hixon10/$PRJ_NAME/$PRJ_NAME:latest
