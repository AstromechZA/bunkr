THIS_DIR = File.dirname(File.expand_path __FILE__)
LICENSE = File.read(File.join(THIS_DIR, 'license.template')).strip

TOP_COMMENT_REGEX = /\A\s*\/\*\*([\s\S]*?)\*\//
COMMENTED_LICENSE = "/**\n" + LICENSE.gsub(/^/, ' * ').gsub(/\s+$/, '') + "\n */\n"

def fix_license_in(path)
    Dir.glob(File.join(path, '**/*.java')).each do |f|

        before_content = File.read(f)

        m = TOP_COMMENT_REGEX.match(before_content)
        if m.nil?
            puts "Fixing missing license in #{f}"
            after_content = COMMENTED_LICENSE + "\n" + before_content
            File.open(f, 'w') do |file|
                file.write(after_content)
            end
        else
            comment_content = m[1].strip.gsub(/^[\* ]*/, '')
            if comment_content != LICENSE
                puts "Fixing incorrect license in #{f}"
                after_content = before_content.sub(TOP_COMMENT_REGEX, COMMENTED_LICENSE)
                File.open(f, 'w') do |file|
                    file.write(after_content)
                end
            end
        end
    end
end
