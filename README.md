# Monorepo workflow for Code Scanning

This sample Actions workflow shows you how to enable filtering results from different Code Scanning runs in the GitHub UI using a workaround.

This is useful when dealing with monorepos that have code for several different projects in the same repository. You can use this workflow modification to mark each project with a unique scanning tool name, and then filter the results in the GitHub Security tab by that tool to only show results for a specific project.

The SARIF is edited before upload to Code Scanning, changing the tool name. CodeQL is also set up so that this doesn't affect the Code Scanning UI.

The tool name can then be used to filter results in the web user interface:

![Filtering results by tool name](./filter-by-tool-name.png)

## Usage

To use this workaround, you need to make some modifications to your Actions workflow file that runs CodeQL.

This example uses a monorepo with Java code, but you can adapt it to your own monorepo.

### The changes in isolation

Change the typical `analyze` step from:

```yaml
    - name: Perform CodeQL Analysis
      uses: github/codeql-action/analyze@v2
      with:
        category: "/language:${{matrix.language}}"
```

to:

```yaml
    - name: Perform CodeQL Analysis
      uses: github/codeql-action/analyze@v2
      with:
        category: "/language:${{matrix.language}}"
        upload: False
        output: sarif-results
      env:
      # https://codeql.github.com/docs/codeql-cli/manual/database-analyze/#options
      # add code snippet, query help and group rules by pack, to the SARIF file
        CODEQL_ACTION_EXTRA_OPTIONS: '{"database":{"analyze":["--sarif-add-snippets","--sarif-add-query-help","--sarif-group-rules-by-pack"]}}'

    # Rename CodeQL tool to allow filtering by workflow in Code Scanning
    - name: Rename CodeQL tool
      run: |
        jq ".runs[0].tool.driver.name = \"CodeQL-${WORKFLOW_TAG}-${{matrix.language}}\"" sarif-results/${{ matrix.language }}.sarif > sarif-results/${{ matrix.language }}-edited.sarif
      env:
        # edit this tag to something unique to your workflow
        WORKFLOW_TAG: "some-unique-tag"  # this will name the tool 'CodeQL-some-unique-tag-java'

    # Upload the CodeQL Analysis results
    - name: Upload SARIF
      uses: github/codeql-action/upload-sarif@v2
      with:
        sarif_file: sarif-results/${{ matrix.language }}-edited.sarif
```

This will rename the CodeQL tool in the SARIF file to include the workflow tag, and uploads the SARIF file to Code Scanning.

The `analyze` step has some changes to ensure that the CodeQL results are displayed properly in the GitHub Advcanced Security UI.

### Full example workflow file

See [`codeql_sample_workflow.yml`](codeql_sample_workflow.yml).
